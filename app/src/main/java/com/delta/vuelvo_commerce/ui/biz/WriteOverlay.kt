package com.delta.vuelvo_commerce.ui.biz

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

/** UI state of the NFC write overlay, driven by the real write session. */
sealed interface WritePhase {
    data object Writing : WritePhase
    data object Success : WritePhase
    data class Error(val message: String) : WritePhase
}

/** NFC write bottom-sheet — mirror of WriteOverlay in vuelvo-biz-write.jsx, driven by [phase]. */
@Composable
fun WriteOverlay(form: TagForm, phase: WritePhase, onClose: () -> Unit) {
    val t = bizTypeById(form.type)
    val success = phase is WritePhase.Success
    val error = phase as? WritePhase.Error
    val dismissable = phase !is WritePhase.Writing

    val context = LocalContext.current
    // Inset de la barra de navegación del sistema: levanta el botón "Hecho" para que no quede tapado.
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Señal acústica + vibración (estilo datáfono) al confirmar la escritura del tag.
    LaunchedEffect(success) {
        if (success) playWriteSuccessFeedback(context)
    }

    // scrim
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF14101E).copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = dismissable,
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
                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 28.dp + navBottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // grabber
            Box(Modifier.width(40.dp).height(5.dp).clip(CircleShape).background(VuStampEmpty))
            Spacer(26.dp)

            WriteTarget(success = success, animate = phase is WritePhase.Writing)

            Spacer(22.dp)
            Text(
                when {
                    success -> "¡Tag listo!"
                    error != null -> "No se pudo escribir"
                    else -> "Escribiendo tag…"
                },
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
                color = if (success) VuAccentDeep else VuInk,
            )
            Spacer(8.dp)
            Text(
                when {
                    success ->
                        "“${form.title.ifBlank { "Comercio" }}” · ${t.label} · ${form.stamps} sellos. El cliente ya puede escanearlo."
                    error != null -> error.message
                    else -> "Acerca el tag NFC a la parte superior del teléfono y mantenlo cerca."
                },
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                color = VuInk2,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            if (dismissable) {
                Spacer(22.dp)
                InkButton(
                    text = if (success) "Hecho" else "Cerrar",
                    icon = null,
                    onClick = onClose,
                )
            }
        }
    }
}

@Composable
private fun WriteTarget(success: Boolean, animate: Boolean) {
    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        if (animate) {
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
                    if (success) Brush.linearGradient(listOf(VuStampHi, VuAccent, VuAccentDeep))
                    else Brush.linearGradient(listOf(Color.White, Color(0xFFF3EEFE)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                success -> Icon(VuelvoIcons.Check, null, tint = Color.White, modifier = Modifier.size(56.dp))
                animate -> Spinner()
                else -> Icon(VuelvoIcons.Nfc, null, tint = VuAccentDeep, modifier = Modifier.size(48.dp))
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

/**
 * Feedback de confirmación al escribir un tag, imitando un pago aceptado en datáfono:
 * un beep claro + una vibración firme. Tolerante a fallos (audio/vibrador no disponibles).
 */
private fun playWriteSuccessFeedback(context: Context) {
    // Vibración firme estilo POS (doble pulso corto + uno largo).
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    runCatching {
        vibrator.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 70, 60, 180), -1),
        )
    }

    // Beep de confirmación. STREAM_MUSIC para que suene aunque las notificaciones estén bajas.
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
        // Liberar el recurso una vez terminado el tono.
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 450)
    }
}
