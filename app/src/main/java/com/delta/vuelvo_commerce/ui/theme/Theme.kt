package com.delta.vuelvo_commerce.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Vuelvo Comercios is a light-only brand surface — no dynamic colour, no dark mode,
// matching the prototype which renders a single fixed light palette.
private val VuelvoColorScheme = lightColorScheme(
    primary = VuAccent,
    onPrimary = VuCard,
    secondary = VuAccentDeep,
    background = VuBg,
    onBackground = VuInk,
    surface = VuCard,
    onSurface = VuInk,
)

@Composable
fun Vuelvo_commerceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VuelvoColorScheme,
        typography = Typography,
        content = content
    )
}
