package com.delta.vuelvo_commerce.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuAccentLine
import com.delta.vuelvo_commerce.ui.theme.VuAccentSoft
import com.delta.vuelvo_commerce.ui.theme.VuStampEmpty
import com.delta.vuelvo_commerce.ui.theme.VuStampHi

/** Columns heuristic for the punch-card grid — mirror of stampCols() in vuelvo-shared.jsx. */
private fun stampCols(max: Int): Int = when {
    max % 5 == 0 -> 5
    max % 6 == 0 -> 6
    max % 4 == 0 -> 4
    else -> 5
}

/**
 * Grid of stamp circles — mirror of the <Stamps> component in vuelvo-shared.jsx.
 * Filled stamps carry a radial accent gradient + white check; the final (reward)
 * empty slot shows a small accent dot.
 */
@Composable
fun Stamps(
    count: Int,
    max: Int,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    gap: Dp = 9.dp,
    accentEmpty: Boolean = false,
) {
    val cols = stampCols(max)
    val rows = (max + cols - 1) / cols
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (c in 0 until cols) {
                    val i = r * cols + c
                    if (i < max) {
                        Stamp(
                            filled = i < count,
                            isReward = i == max - 1,
                            size = size,
                            accentEmpty = accentEmpty,
                        )
                    } else {
                        Box(Modifier.size(size))
                    }
                }
            }
        }
    }
}

@Composable
private fun Stamp(filled: Boolean, isReward: Boolean, size: Dp, accentEmpty: Boolean) {
    val base = Modifier.size(size).clip(CircleShape)
    if (filled) {
        Box(
            base.background(
                Brush.radialGradient(
                    0f to VuStampHi,
                    0.48f to VuAccent,
                    1f to VuAccentDeep,
                )
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                VuelvoIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    } else {
        Box(
            base
                .background(if (accentEmpty) VuAccentSoft else Color.Transparent)
                .border(1.6.dp, VuStampEmpty, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isReward) {
                Box(
                    Modifier
                        .size(size * 0.26f)
                        .clip(CircleShape)
                        .background(VuAccentLine)
                )
            }
        }
    }
}
