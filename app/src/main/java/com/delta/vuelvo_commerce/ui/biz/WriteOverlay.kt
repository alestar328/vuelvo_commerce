package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuAccentSoft
import com.delta.vuelvo_commerce.ui.theme.VuCard
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk2
import com.delta.vuelvo_commerce.ui.theme.VuStampEmpty
import com.delta.vuelvo_commerce.ui.theme.VuStampHi
import kotlinx.coroutines.delay

/** NFC write bottom-sheet animation — mirror of WriteOverlay in vuelvo-biz-write.jsx. */
@Composable
fun WriteOverlay(form: TagForm, onClose: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    val t = bizTypeById(form.type)

    LaunchedEffect(Unit) {
        delay(1700)
        done = true
    }

    // scrim
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF14101E).copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = done,
                onClick = onClose,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(VuCard)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // grabber
            Box(Modifier.width(40.dp).height(5.dp).clip(CircleShape).background(VuStampEmpty))
            Spacer(26.dp)

            WriteTarget(done = done)

            Spacer(22.dp)
            Text(
                if (done) "¡Tag listo!" else "Escribiendo tag…",
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
                color = if (done) VuAccentDeep else VuInk,
            )
            Spacer(8.dp)
            Text(
                if (done) {
                    "“${form.title.ifBlank { "Comercio" }}” · ${t.label} · ${form.stamps} sellos. El cliente ya puede escanearlo."
                } else {
                    "Acerca el tag NFC a la parte superior del iPhone y mantenlo cerca."
                },
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                color = VuInk2,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            if (done) {
                Spacer(22.dp)
                InkButton(text = "Hecho", icon = null, onClick = onClose)
            }
        }
    }
}

@Composable
private fun WriteTarget(done: Boolean) {
    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        if (!done) {
            // expanding rings
            val transition = rememberInfiniteTransition(label = "rings")
            listOf(0, 1, 2).forEach { idx ->
                val p by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, delayMillis = idx * 533, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "ring$idx",
                )
                Box(
                    Modifier
                        .size(180.dp)
                        .scale(0.55f + p * 1.05f)
                        .drawBehind {
                            drawCircle(
                                color = VuAccent.copy(alpha = ((1f - p) * 0.5f).coerceIn(0f, 0.5f)),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                )
            }
        }

        // core disc
        Box(
            Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(
                    if (done) Brush.linearGradient(listOf(VuStampHi, VuAccent, VuAccentDeep))
                    else Brush.linearGradient(listOf(Color.White, Color(0xFFF3EEFE)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(VuelvoIcons.Check, null, tint = Color.White, modifier = Modifier.size(56.dp))
            } else {
                Spinner()
            }
        }
    }
}

@Composable
private fun Spinner() {
    val transition = rememberInfiniteTransition(label = "spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "angle",
    )
    Box(
        Modifier
            .size(46.dp)
            .rotate(angle)
            .drawBehind {
                drawCircle(color = VuAccentSoft, style = Stroke(width = 4.dp.toPx()))
                drawArc(
                    color = VuAccent,
                    startAngle = -90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
    )
}

@Composable
private fun Spacer(size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.size(size))
}
