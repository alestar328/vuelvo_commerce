package com.delta.vuelvo_commerce.ui.biz

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.text.Normalizer

/**
 * Domain helpers for the tag configuration. The UI keeps editing [TagForm]; these computed
 * properties derive the values the NFC writer needs (business slug + deeplink), mirroring the
 * `TagConfig` model from the original spec.
 */

/**
 * Recompensa que se escribe en el tag: la que el comercio ha escrito a mano en el formulario y, si
 * la ha dejado en blanco, el texto por defecto del tipo de establecimiento. Nunca va vacía — es lo
 * que la app del cliente pinta en la tarjeta y en el premio que emite al completarla.
 */
val TagForm.effectiveReward: String
    get() = reward.trim().ifBlank { bizTypeById(type).reward }

/**
 * Teléfono en formato internacional (`+34 600123456`), o cadena vacía si el comercio no puso ninguno.
 * Vuelvo se usa en varios países, así que el prefijo viaja **siempre** en el tag: sin él la app del
 * cliente tendría que adivinar el país para poder abrir WhatsApp.
 *
 * El 0 inicial del número nacional se quita salvo donde forma parte del número internacional (ver
 * [DialCountry.keepsTrunkZero]) — es prefijo de larga distancia interna y WhatsApp lo rechaza.
 */
val TagForm.internationalPhone: String
    get() {
        val country = dialCountryByIso(phoneCc)
        val local = phone.trim().let { if (country.keepsTrunkZero()) it else it.trimStart('0', ' ') }
        return if (local.isBlank()) "" else "+${country.dial} $local"
    }

/** Slug of the title: diacritics stripped, lowercase, spaces → hyphen. */
val TagForm.businessID: String
    get() = slugify(title)

/**
 * Deeplink the consumer app opens when scanning the tag (URL-encoded by [Uri.Builder]):
 * `vuelvo://stamp?code=…&id=…&name=…&cat=…&sym=…&color=…&tile=…&ink=…&max=…&reward=…&addr=…&tel=…&logo=…&cover=…`
 *
 * Card colour is written **twice** so it survives the trip to iOS:
 *  - `color` is the shared palette id (e.g. `violet`). Both apps ship the same [CardColors] table, so
 *    this is the canonical, platform-agnostic token — iOS looks the id up and resolves its own colours.
 *  - `tile` / `ink` are the resolved RRGGBB hexes, a fallback for any client that doesn't know the id.
 *
 * `code` is this comercio's `businessCode` — the **sole identifier** actually used by the "comercio
 * activo" model: the key of its `businesses/{code}` Firestore record (see
 * [com.delta.vuelvo_commerce.data.BusinessRegistryRepository]) and the basis for Storage object names.
 * The client app reads `businesses/{code}.active` before applying a stamp; a comercio without one (or
 * with `active == false`) is treated as unavailable. [deviceUuid] (the installation id) rides along
 * purely for forward compatibility — nothing reads or depends on it today, it's just kept on the tag in
 * case a future feature needs it.
 *
 * `addr` / `tel` are the merchant's contact details, written only when it filled them in. The consumer
 * app shows them in the card-detail header, so the customer knows where to redeem the reward. `tel`
 * always carries the country prefix ([internationalPhone]) — Vuelvo is used in several countries, so
 * the client must never have to guess one to place a call or open WhatsApp.
 *
 * [logoRef] / [coverRef] are the flat Firebase Storage object names (no folders, no extension) of the
 * images uploaded right before this call — see [imageRef] and
 * [com.delta.vuelvo_commerce.data.ImageUploadRepository] — or null when the merchant set no image. The
 * consumer app builds the download URL as
 * `https://firebasestorage.googleapis.com/v0/b/<bucket>/o/<value>.jpg?alt=media` with its own hardcoded
 * bucket — never the actual signed download URL, which runs 150-200+ chars once percent-encoded as a
 * query value and alone exceeds most NFC tags' capacity.
 */
fun TagForm.deeplinkUrl(
    businessCode: String,
    deviceUuid: String,
    logoRef: String? = null,
    coverRef: String? = null,
): String {
    val biz = bizTypeById(type)
    val card = cardColorById(color)
    return Uri.Builder()
        .scheme("vuelvo")
        .authority("stamp")
        .appendQueryParameter("code", businessCode)
        .appendQueryParameter("uuid", deviceUuid)
        .appendQueryParameter("id", businessID)
        .appendQueryParameter("name", title)
        .appendQueryParameter("cat", biz.label)
        .appendQueryParameter("sym", biz.sym)
        .appendQueryParameter("color", card.id)
        .appendQueryParameter("tile", card.tile.toHex())
        .appendQueryParameter("ink", card.ink.toHex())
        .appendQueryParameter("max", stamps.toString())
        .appendQueryParameter("reward", effectiveReward)
        .apply {
            // Datos de contacto: solo viajan si el comercio los ha rellenado — el tag NFC va justo de
            // capacidad y la app del cliente ya sabe pintar la cabecera sin ellos.
            address.trim().takeIf { it.isNotBlank() }?.let { appendQueryParameter("addr", it) }
            internationalPhone.takeIf { it.isNotBlank() }?.let { appendQueryParameter("tel", it) }
            logoRef?.let { appendQueryParameter("logo", it) }
            coverRef?.let { appendQueryParameter("cover", it) }
        }
        .build()
        .toString()
}

/**
 * Flat Firebase Storage object name for one of this comercio's images — `{businessID}_logo` /
 * `{businessID}_cover`, [kind] being `"logo"` or `"cover"`.
 *
 * Keyed on the **comercio**, matching the model: one comercio owns one tag, so re-writing that tag is
 * meant to replace its images, and pointing at a stable name makes the overwrite happen by itself. It
 * is deliberately *not* keyed on the device id — the id identifies the install, so two merchant phones
 * each writing their own tag ended up sharing a single pair of objects and every card rendered the last
 * images uploaded.
 *
 * [businessID] is the same slug the tag carries as `id=`, i.e. the identity the consumer app already
 * uses to tell one card from another, so images can't collide any more than cards themselves can. It
 * falls back to [businessCode] when the merchant left the title empty, so that blank titles don't all
 * land on one shared object (such a tag is broken anyway — the consumer app rejects an empty `id=`).
 */
fun TagForm.imageRef(kind: String, businessCode: String): String =
    "${businessID.ifBlank { businessCode }}_$kind"

/** Color as a 6-digit uppercase RRGGBB hex string (no `#`, no alpha), e.g. `F8E6EE`. */
private fun Color.toHex(): String = "%06X".format(0xFFFFFF and toArgb())

private val combiningMarks = Regex("\\p{Mn}+")
private val nonSlugChars = Regex("[^a-z0-9]+")

private fun slugify(input: String): String {
    val withoutDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
        .replace(combiningMarks, "")
    return withoutDiacritics
        .lowercase()
        .replace(nonSlugChars, "-")
        .trim('-')
}
