package com.delta.vuelvo_commerce

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.delta.vuelvo_commerce.ui.biz.TagImageCodec
import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Round-trip check for [TagImageCodec]: a transparent PNG (circular logo) must survive
 * encode→decode with its alpha channel intact — corners stay transparent, never white.
 */
@RunWith(AndroidJUnit4::class)
class TagImageCodecTest {

    @Test
    fun transparentPngKeepsAlphaThroughRoundTrip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Circular opaque logo on a fully transparent 96x96 canvas, saved as PNG.
        val src = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        Canvas(src).drawCircle(48f, 48f, 40f, Paint().apply { color = Color.RED })
        val file = File(context.cacheDir, "logo_alpha_test.png")
        file.outputStream().use { src.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val encoded = TagImageCodec.encode(context, Uri.fromFile(file), TagImageCodec.LOGO)
        assertNotNull("encode returned null", encoded)

        val decoded = TagImageCodec.decode(encoded!!)
        assertNotNull("decode returned null", decoded)

        assertTrue("decoded bitmap lost its alpha channel", decoded!!.hasAlpha())
        val cornerAlpha = Color.alpha(decoded.getPixel(2, 2))
        assertTrue("corner should be transparent but alpha=$cornerAlpha", cornerAlpha < 32)
        val centerAlpha = Color.alpha(decoded.getPixel(48, 48))
        assertTrue("center should be opaque but alpha=$centerAlpha", centerAlpha > 224)
    }
}
