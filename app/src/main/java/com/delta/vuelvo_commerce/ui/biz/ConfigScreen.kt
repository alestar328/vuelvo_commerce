package com.delta.vuelvo_commerce.ui.biz

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.components.Stamps
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuAccentLine
import com.delta.vuelvo_commerce.ui.theme.VuAccentSoft
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.VuCard
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk2
import com.delta.vuelvo_commerce.ui.theme.VuInk3
import com.delta.vuelvo_commerce.ui.theme.VuLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            // ── Logo / foto del comercio ─────────────
            Column {
                FieldLabel("Foto del comercio", hint = "opcional")
                PhotoField(
                    encoded = form.logo,
                    profile = TagImageCodec.LOGO,
                    addTitle = "Subir foto o logo",
                    addHint = "JPG o PNG · opcional",
                    setTitle = "Foto añadida",
                    setHint = "Aparecerá en la tarjeta del cliente",
                    onChange = { onForm(form.copy(logo = it)) },
                )
            }

            // ── Fondo de la tarjeta ──────────────────
            Column {
                FieldLabel("Fondo de la tarjeta", hint = "opcional")
                PhotoField(
                    encoded = form.cover,
                    profile = TagImageCodec.COVER,
                    addTitle = "Subir imagen de fondo",
                    addHint = "Se verá detrás del nombre · opcional",
                    setTitle = "Fondo añadido",
                    setHint = "Cabecera de la tarjeta del cliente",
                    onChange = { onForm(form.copy(cover = it)) },
                )
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

            // ── Color de la tarjeta ──────────────────
            Column {
                FieldLabel("Color de la tarjeta")
                ColorField(form.color) { onForm(form.copy(color = it)) }
            }

            // ── Vista previa ─────────────────────────
            Column {
                FieldLabel("Vista previa")
                LivePreview(form.title, form.type, form.stamps, form.logo, form.color, form.cover)
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

/** True when the image carries an alpha channel — those logos render without frame or crop. */
private fun ImageBitmap.hasTransparency(): Boolean = asAndroidBitmap().hasAlpha()

/** Decodes a Base64 image string into an [ImageBitmap] off the main thread, recomputing on change. */
@Composable
private fun rememberDecoded(encoded: String?): ImageBitmap? {
    val state = produceState<ImageBitmap?>(initialValue = null, key1 = encoded) {
        value = if (encoded == null) null
        else withContext(Dispatchers.Default) { TagImageCodec.decode(encoded)?.asImageBitmap() }
    }
    return state.value
}

/**
 * Upload control for a single image — mirror of PhotoField in vuelvo-biz-config.jsx. Picks an image
 * with the system photo picker (no runtime permission needed), encodes it to a compact Base64 string
 * via [TagImageCodec] and reports it through [onChange]; the empty/filled states match the prototype.
 */
@Composable
private fun PhotoField(
    encoded: String?,
    profile: TagImageCodec.Profile,
    addTitle: String,
    addHint: String,
    setTitle: String,
    setHint: String,
    onChange: (String?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bitmap = rememberDecoded(encoded)

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) { TagImageCodec.encode(context, uri, profile) }
                onChange(result)
            }
        }
    }
    fun pick() = picker.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
    )

    if (encoded != null) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VuCard)
                .border(1.5.dp, VuLine, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(Modifier.size(54.dp).clip(RoundedCornerShape(13.dp)).background(VuBg)) {
                bitmap?.let {
                    Image(
                        it,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = if (it.hasTransparency()) ContentScale.Fit else ContentScale.Crop,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(setTitle, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = VuInk)
                Text(setHint, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = VuInk3)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(VuAccentSoft)
                    .clickable { pick() }
                    .padding(horizontal = 13.dp, vertical = 8.dp),
            ) {
                Text("Cambiar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VuAccentDeep)
            }
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(VuBg)
                    .clickable { onChange(null) },
                contentAlignment = Alignment.Center,
            ) {
                Text("×", fontSize = 19.sp, fontWeight = FontWeight.Medium, color = VuInk2)
            }
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(VuCard)
                .border(1.5.dp, VuAccentLine, RoundedCornerShape(16.dp))
                .clickable { pick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(VuAccentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(VuelvoIcons.Camera, contentDescription = null, tint = VuAccentDeep, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(addTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VuInk)
                Text(addHint, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = VuInk3)
            }
            Icon(VuelvoIcons.Plus, contentDescription = null, tint = VuInk3, modifier = Modifier.size(20.dp))
        }
    }
}

/** Card colour swatch picker — mirror of ColorField in vuelvo-biz-config.jsx. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorField(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        CardColors.forEach { c ->
            val active = c.id == selected
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(c.tile, VuCard)))
                    .border(
                        if (active) 2.5.dp else 1.5.dp,
                        if (active) c.ink else VuLine,
                        RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelect(c.id) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(18.dp).clip(CircleShape).background(c.ink),
                    contentAlignment = Alignment.Center,
                ) {
                    if (active) {
                        Icon(VuelvoIcons.Check, contentDescription = c.label, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePreview(
    title: String,
    type: String,
    stamps: Int,
    logo: String?,
    color: String,
    cover: String?,
) {
    val t = bizTypeById(type)
    val c = cardColorById(color)
    val logoBitmap = rememberDecoded(logo)
    val coverBitmap = rememberDecoded(cover)
    val hasCover = coverBitmap != null

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .then(
                    if (hasCover) Modifier.background(Color(0xFF0E0B16))
                    else Modifier.background(Brush.linearGradient(listOf(c.tile, VuCard)))
                )
                .border(1.dp, VuLine, RoundedCornerShape(24.dp)),
        ) {
            // full-bleed blurred cover + darkening scrim
            if (coverBitmap != null) {
                Image(
                    coverBitmap,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize().blur(3.dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x570F0B16), Color(0x800F0B16)),
                            )
                        )
                )
            }

            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Logos con transparencia (círculos, wordmarks…) van sin marco ni recorte;
                    // solo las fotos opacas conservan el cuadrado redondeado de fondo.
                    val logoAlpha = logoBitmap != null && logoBitmap.hasTransparency()
                    Box(
                        Modifier
                            .size(46.dp)
                            .then(
                                if (logoAlpha) Modifier
                                else Modifier.clip(RoundedCornerShape(14.dp)).background(VuCard)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                logoBitmap,
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = if (logoAlpha) ContentScale.Fit else ContentScale.Crop,
                            )
                        } else {
                            Icon(t.icon, contentDescription = null, tint = c.ink, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            title.ifBlank { "Nombre del comercio" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            color = if (hasCover) Color.White else VuInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            t.label,
                            fontSize = 13.5.sp,
                            color = if (hasCover) Color.White.copy(alpha = 0.9f) else VuInk2,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                val stampSize = (26 - maxOf(0, stamps - 10) * 1.1).coerceIn(9.0, 26.0)
                if (hasCover) {
                    // frosted panel so the stamps stay legible over the cover
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                    ) {
                        Stamps(count = 0, max = stamps, size = stampSize.dp, gap = 8.dp)
                    }
                } else {
                    Stamps(count = 0, max = stamps, size = stampSize.dp, gap = 8.dp, accentEmpty = true)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
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
