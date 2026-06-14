package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.components.Stamps
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.VuCard
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk2
import com.delta.vuelvo_commerce.ui.theme.VuInk3
import com.delta.vuelvo_commerce.ui.theme.VuLine

val AccentGradient = Brush.linearGradient(listOf(VuAccent, VuAccentDeep))

@Composable
fun ConfigScreen(
    form: TagForm,
    onForm: (TagForm) -> Unit,
    subscribed: Boolean,
    onWrite: () -> Unit,
    onGoPaywall: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 150.dp)) {
        BizHeader(
            title = "Nuevo tag",
            subtitle = "Configura los datos y escríbelos en el tag NFC del comercio.",
        )

        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            // ── Título ───────────────────────────────
            Column {
                FieldLabel("Título del comercio")
                TitleField(form.title) { onForm(form.copy(title = it.take(28))) }
            }

            // ── Tipo ─────────────────────────────────
            Column {
                FieldLabel("Tipo de establecimiento")
                TypeGrid(form.type) { onForm(form.copy(type = it)) }
            }

            // ── Sellos ───────────────────────────────
            Column {
                FieldLabel("Sellos para la recompensa", hint = "2 – 50")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VuCard)
                        .border(1.5.dp, VuLine, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Stepper(form.stamps, 2, 50) { onForm(form.copy(stamps = it)) }
                }
            }

            // ── Vista previa ─────────────────────────
            Column {
                FieldLabel("Vista previa")
                LivePreview(form.title, form.type, form.stamps)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── CTA ──────────────────────────────────────
        if (subscribed) {
            GradientButton(text = "Escribir en el tag NFC", icon = VuelvoIcons.Nfc, onClick = onWrite)
        } else {
            InkButton(text = "Activa tu suscripción para escribir", icon = VuelvoIcons.Lock, onClick = onGoPaywall)
            Spacer(Modifier.height(10.dp))
            Text(
                "La escritura de tags requiere un plan activo",
                Modifier.fillMaxWidth(),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = VuInk3,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TitleField(value: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(VuCard)
            .border(1.5.dp, VuLine, RoundedCornerShape(15.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text("Ej. Cafè Nostrum", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VuInk3)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VuInk),
            cursorBrush = SolidColor(VuAccent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TypeGrid(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BizTypes.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t ->
                    TypeCell(t, t.id == selected, Modifier.weight(1f)) { onSelect(t.id) }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TypeCell(t: BizType, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VuCard)
            .border(
                if (active) 2.dp else 1.5.dp,
                if (active) VuAccent else VuLine,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(t.tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(t.icon, contentDescription = null, tint = t.ink, modifier = Modifier.size(21.dp))
        }
        Text(
            t.label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) VuAccentDeep else VuInk2,
        )
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        StepBtn(VuelvoIcons.Minus, value <= min) { onChange(value - 1) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, color = VuInk, lineHeight = 30.sp)
            Spacer(Modifier.height(3.dp))
            Text("sellos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VuInk3)
        }
        StepBtn(VuelvoIcons.Plus, value >= max) { onChange(value + 1) }
    }
}

@Composable
private fun StepBtn(icon: ImageVector, disabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (disabled) VuBg else VuCard)
            .border(1.5.dp, VuLine, RoundedCornerShape(13.dp))
            .then(if (disabled) Modifier else Modifier.clickable(onClick = onClick))
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (disabled) VuInk3 else VuInk, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LivePreview(title: String, type: String, stamps: Int) {
    val t = bizTypeById(type)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(t.tile, VuCard)))
            .border(1.dp, VuLine, RoundedCornerShape(24.dp))
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(VuCard),
                contentAlignment = Alignment.Center,
            ) {
                Icon(t.icon, contentDescription = null, tint = t.ink, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title.ifBlank { "Nombre del comercio" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = VuInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(t.label, fontSize = 13.5.sp, color = VuInk2, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(16.dp))
        val stampSize = (26 - maxOf(0, stamps - 10) * 1.1).coerceIn(9.0, 26.0)
        Stamps(count = 0, max = stamps, size = stampSize.dp, gap = 8.dp, accentEmpty = true)
        Spacer(Modifier.height(14.dp))
        Text("Vista previa de la tarjeta del cliente", fontSize = 12.5.sp, color = VuInk3, fontWeight = FontWeight.SemiBold)
    }
}

// ── Shared buttons ───────────────────────────────────────────
@Composable
fun GradientButton(text: String, icon: ImageVector?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AccentGradient)
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
fun InkButton(text: String, icon: ImageVector?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VuInk)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
