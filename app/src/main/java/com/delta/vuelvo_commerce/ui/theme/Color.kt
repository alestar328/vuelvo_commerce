package com.delta.vuelvo_commerce.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vuelvo Comercios design tokens — mirror of the `:root` custom properties in
 * vuelvo-shared.jsx. The accent can be re-themed (the prototype offered a few
 * swatches via the tweaks panel); the deep / soft / line variants are derived
 * from it exactly as the prototype did.
 */

// Accent (default brand purple)
val VuAccent = Color(0xFF9B5CFF)
val VuAccentDeep = Color(0xFF7B3CE6)
val VuAccentSoft = Color(0x1F9B5CFF) // accent @ ~12%
val VuAccentLine = Color(0x479B5CFF) // accent @ ~28%

// Surfaces
val VuBg = Color(0xFFF5F4F8)
val VuCard = Color(0xFFFFFFFF)
val VuDeviceBg = Color(0xFFECEAF2)

// Ink (text)
val VuInk = Color(0xFF1B1726)
val VuInk2 = Color(0xFF6E6880)
val VuInk3 = Color(0xFFA7A1B5)

// Lines / stamps
val VuLine = Color(0x121B1726) // rgba(27,23,38,.07)
val VuStampEmpty = Color(0xFFE9E6F0)

// Stamp gradient highlight
val VuStampHi = Color(0xFFB083FF)
