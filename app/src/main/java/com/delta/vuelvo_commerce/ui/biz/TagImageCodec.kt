package com.delta.vuelvo_commerce.ui.biz

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Turns a picked image into a compact Base64 **string** and back, so logos and card backgrounds can
 * travel as plain text — in the live preview, in the write overlay and, crucially, inside the NFC
 * tag payload (the deeplink written by [com.delta.vuelvo_commerce.ui.biz.deeplinkUrl]).
 *
 * NFC tags are tiny (NTAG213 ≈ 137 usable bytes, NTAG215 ≈ 492, NTAG216 ≈ 868), so the bitmaps are
 * downscaled hard and JPEG-compressed before encoding. Even so the encoded logo+cover only fit on
 * roomy tags; [TagImageCodec.LOGO] / [TagImageCodec.COVER] expose the budgets to tune per tag.
 */
object TagImageCodec {

    /** Encoding profile: longest edge in px + JPEG quality. Smaller = fits on more tags. */
    data class Profile(val maxDimPx: Int, val quality: Int)

    /** Logo lives in a ~46 dp avatar — a small square thumbnail is plenty. */
    val LOGO = Profile(maxDimPx = 96, quality = 70)

    /** Card background is shown blurred behind the header, so it can stay low-res. */
    val COVER = Profile(maxDimPx = 220, quality = 60)

    private const val BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP

    /**
     * Decodes the image at [uri], honouring EXIF rotation, downscales it to [profile] and returns it
     * as a url-safe Base64 JPEG string (no `data:` prefix, to save tag space). Null on any failure.
     */
    fun encode(context: Context, uri: Uri, profile: Profile): String? = runCatching {
        val source = decodeSampled(context, uri, profile.maxDimPx) ?: return null
        val oriented = applyExifOrientation(context, uri, source)
        val scaled = scaleToMaxDim(oriented, profile.maxDimPx)
        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, profile.quality, out)
            out.toByteArray()
        }
        if (scaled !== oriented) scaled.recycle()
        if (oriented !== source) oriented.recycle()
        source.recycle()
        Base64.encodeToString(bytes, BASE64_FLAGS)
    }.getOrNull()

    /** Decodes a string produced by [encode] back into a [Bitmap]. Null if the string is invalid. */
    fun decode(encoded: String): Bitmap? = runCatching {
        val bytes = Base64.decode(encoded, BASE64_FLAGS)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()

    /** Approximate size in bytes that [encoded] adds to the NFC payload once URL-encoded. */
    fun encodedByteSize(encoded: String): Int = encoded.length

    private fun decodeSampled(context: Context, uri: Uri, maxDimPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while (largest / (sample * 2) >= maxDimPx) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    }

    private fun scaleToMaxDim(src: Bitmap, maxDimPx: Int): Bitmap {
        val largest = maxOf(src.width, src.height)
        if (largest <= maxDimPx) return src
        val ratio = maxDimPx.toFloat() / largest
        return Bitmap.createScaledBitmap(
            src,
            (src.width * ratio).toInt().coerceAtLeast(1),
            (src.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
