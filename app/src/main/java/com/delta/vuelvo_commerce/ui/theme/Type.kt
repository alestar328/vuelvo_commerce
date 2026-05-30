package com.delta.vuelvo_commerce.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The prototype uses Plus Jakarta Sans (weights 400–800). To keep the build
 * fully offline we fall back to the platform sans-serif and lean on the same
 * weight scale — swap [VuFont] for a bundled/downloadable Plus Jakarta Sans
 * family to make it pixel-exact.
 */
val VuFont: FontFamily = FontFamily.Default

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = VuFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
)
