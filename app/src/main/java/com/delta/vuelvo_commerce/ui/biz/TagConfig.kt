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
 * `vuelvo://stamp?id=…&name=…&cat=…&sym=…&tile=…&ink=…&max=…&reward=…`
 */
val TagForm.deeplinkUrl: String
    get() {
        val biz = bizTypeById(type)
        return Uri.Builder()
            .scheme("vuelvo")
            .authority("stamp")
            .appendQueryParameter("id", businessID)
            .appendQueryParameter("name", title)
            .appendQueryParameter("cat", biz.label)
            .appendQueryParameter("sym", biz.sym)
            .appendQueryParameter("tile", biz.tile.toHex())
            .appendQueryParameter("ink", biz.ink.toHex())
            .appendQueryParameter("max", stamps.toString())
            .appendQueryParameter("reward", biz.reward)
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
