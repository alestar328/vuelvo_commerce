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

/** Default reward message for the selected establishment type. */
val TagForm.reward: String
    get() = bizTypeById(type).reward

/** Slug of the title: diacritics stripped, lowercase, spaces → hyphen. */
val TagForm.businessID: String
    get() = slugify(title)

/**
 * Deeplink the consumer app opens when scanning the tag (URL-encoded by [Uri.Builder]):
 * `vuelvo://stamp?id=…&name=…&cat=…&sym=…&color=…&tile=…&ink=…&max=…&reward=…&uuid=…&logo=…&cover=…`
 *
 * Card colour is written **twice** so it survives the trip to iOS:
 *  - `color` is the shared palette id (e.g. `violet`). Both apps ship the same [CardColors] table, so
 *    this is the canonical, platform-agnostic token — iOS looks the id up and resolves its own colours.
 *  - `tile` / `ink` are the resolved RRGGBB hexes, a fallback for any client that doesn't know the id.
 *
 * `uuid` is this install's device id (see [com.delta.vuelvo_commerce.data.DeviceIdStore]); it ties a
 * scanned card back to the merchant that wrote it.
 *
 * [hasLogo] / [hasCover] flag whether an image was uploaded to Firebase Storage right before this call
 * (see [com.delta.vuelvo_commerce.data.ImageUploadRepository]) — when true, `logo`/`cover` carry the flat
 * object name `{deviceUuid}_logo` / `{deviceUuid}_cover` (no folders, no extension), matching exactly
 * what was uploaded. A future client-side reader builds the download URL as
 * `https://firebasestorage.googleapis.com/v0/b/<bucket>/o/<value>.jpg?alt=media` with its own hardcoded
 * bucket — never the actual signed download URL, which runs 150-200+ chars once percent-encoded as a
 * query value and alone exceeds most NFC tags' capacity.
 */
fun TagForm.deeplinkUrl(deviceUuid: String, hasLogo: Boolean = false, hasCover: Boolean = false): String {
    val biz = bizTypeById(type)
    val card = cardColorById(color)
    return Uri.Builder()
        .scheme("vuelvo")
        .authority("stamp")
        .appendQueryParameter("id", businessID)
        .appendQueryParameter("name", title)
        .appendQueryParameter("cat", biz.label)
        .appendQueryParameter("sym", biz.sym)
        .appendQueryParameter("color", card.id)
        .appendQueryParameter("tile", card.tile.toHex())
        .appendQueryParameter("ink", card.ink.toHex())
        .appendQueryParameter("max", stamps.toString())
        .appendQueryParameter("reward", biz.reward)
        .appendQueryParameter("uuid", deviceUuid)
        .apply {
            if (hasLogo) appendQueryParameter("logo", "${deviceUuid}_logo")
            if (hasCover) appendQueryParameter("cover", "${deviceUuid}_cover")
        }
        .build()
        .toString()
}

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
