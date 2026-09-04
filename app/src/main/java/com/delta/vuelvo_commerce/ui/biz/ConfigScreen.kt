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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
    onWrite: () -> Unit,
    businessCode: String,
    onBusinessCode: (String) -> Unit,
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
                TextFieldBox(
                    value = form.title,
                    placeholder = "Ej. Cafè Nostrum",
                    // A comercio name is a proper noun: open the keyboard shifted so it starts capitalised.
                    capitalization = KeyboardCapitalization.Sentences,
                ) { onForm(form.copy(title = it.take(28))) }
            }

            // ── Código de comercio ───────────────────
            // Identificador local persistido en el dispositivo (campo informativo dentro del registro
            // `businesses/{uuid}` en Firestore, no la clave) — editable para que el comercio pueda tener
            // uno memorable/legible a mano en la consola de Firebase.
            Column {
                FieldLabel("Código de comercio", hint = "identificador interno")
                TextFieldBox(
                    value = businessCode,
                    placeholder = "Ej. 042817",
                    onChange = onBusinessCode,
                )
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
            // Foto o color, nunca los dos: subir una foto de fondo desactiva el color de abajo, y
            // elegir un color de abajo retira la foto.
            Column {
                FieldLabel("Fondo de la tarjeta", hint = "opcional")
                PhotoField(
                    encoded = form.cover,
                    profile = TagImageCodec.COVER,
                    addTitle = "Subir imagen de fondo",
                    addHint = "Sustituye al color de la tarjeta · opcional",
                    setTitle = "Fondo añadido",
                    setHint = "La tarjeta usa la foto en vez del color",
                    onChange = { onForm(form.copy(cover = it)) },
                )
            }

            // ── Tipo ─────────────────────────────────
            Column {
                FieldLabel("Tipo de establecimiento")
                // Cambiar de tipo arrastra la recompensa por defecto del nuevo tipo, pero solo mientras
                // el comercio no haya escrito la suya: un texto propio no se pisa nunca.
                TypeGrid(form.type) { picked ->
                    val untouched = form.reward.isBlank() || form.reward == bizTypeById(form.type).reward
                    onForm(
                        form.copy(
                            type = picked,
                            reward = if (untouched) bizTypeById(picked).reward else form.reward,
                        )
                    )
                }
            }

            // ── Recompensa ───────────────────────────
            // Lo que el cliente se lleva al completar la tarjeta. Viaja en el tag (`reward=`) y la app
            // del cliente lo pinta en la tarjeta y en el premio que emite al completarla; en blanco se
            // usa el texto por defecto del tipo de establecimiento.
            Column {
                FieldLabel("Recompensa", hint = "aparece en la tarjeta")
                TextFieldBox(
                    value = form.reward,
                    placeholder = bizTypeById(form.type).reward,
                    capitalization = KeyboardCapitalization.Sentences,
                ) { onForm(form.copy(reward = it.take(40))) }
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
                val hasCover = form.cover != null
                FieldLabel(
                    "Color de la tarjeta",
                    hint = if (hasCover) "sustituido por la foto" else null,
                )
                // Tocar un color retira la foto de fondo: solo uno de los dos pinta la tarjeta.
                ColorField(form.color, dimmed = hasCover) {
                    onForm(form.copy(color = it, cover = null))
                }
            }

            // ── Datos de contacto ────────────────────
            // Opcionales: viajan en el tag como `addr=`/`tel=` y la app del cliente los muestra en la
            // cabecera del detalle de la tarjeta, para que el cliente sepa dónde canjear el premio.
            Column {
                FieldLabel("Datos de contacto", hint = "opcional")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextFieldBox(
                        value = form.address,
                        placeholder = "Dirección · Ej. Carrer Major 12",
                        capitalization = KeyboardCapitalization.Sentences,
                    ) { onForm(form.copy(address = it.take(60))) }
                    // El prefijo se elige, no se asume: hay comercios fuera de España y el cliente
                    // necesita el número internacional para poder abrir WhatsApp.
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CountryCodeField(form.phoneCc) { onForm(form.copy(phoneCc = it)) }
                        Box(Modifier.weight(1f)) {
                            TextFieldBox(
                                value = form.phone,
                                placeholder = "Teléfono · Ej. 600 123 456",
                                keyboardType = KeyboardType.Phone,
                            ) { onForm(form.copy(phone = it.take(20))) }
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    "Se muestran en la cabecera de la tarjeta del cliente. Escribe el teléfono sin el " +
                        "prefijo del país: ese se elige al lado.",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VuInk3,
                )
            }

            // ── Vista previa ─────────────────────────
            Column {
                FieldLabel("Vista previa")
                LivePreview(
                    title = form.title,
                    type = form.type,
                    stamps = form.stamps,
                    logo = form.logo,
                    color = form.color,
                    cover = form.cover,
                    reward = form.effectiveReward,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── CTA ──────────────────────────────────────
        // The title is the comercio's identity: it becomes the tag's `id=` and names its images in
        // Storage. Blank means a tag the customer app silently rejects, so don't let it be written.
        val hasTitle = form.title.isNotBlank()
        GradientButton(
            text = "Escribir en el tag NFC",
            icon = VuelvoIcons.Nfc,
            onClick = onWrite,
            enabled = hasTitle,
        )
        if (!hasTitle) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Escribe el título del comercio para poder grabar el tag",
                Modifier.fillMaxWidth(),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = VuInk3,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/**
 * Caja de texto de una línea con el aspecto de los campos del handoff (tarjeta, borde y placeholder).
 * Única para todos los campos de la pantalla: título, código, recompensa y datos de contacto.
 */
@Composable
private fun TextFieldBox(
    value: String,
    placeholder: String,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
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
            Text(
                placeholder,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VuInk3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VuInk),
            cursorBrush = SolidColor(VuAccent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Prefijo internacional del teléfono: pastilla con bandera y prefijo que despliega la lista de
 * países. Va pegado al campo del teléfono, del que solo separa el prefijo.
 */
@Composable
private fun CountryCodeField(iso: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val country = dialCountryByIso(iso)
    Box {
        Row(
            Modifier
                .clip(RoundedCornerShape(15.dp))
                .background(VuCard)
                .border(1.5.dp, VuLine, RoundedCornerShape(15.dp))
                .clickable { open = true }
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "${country.flag} +${country.dial}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VuInk,
                maxLines = 1,
            )
            Icon(VuelvoIcons.ChevronDown, contentDescription = null, tint = VuInk3, modifier = Modifier.size(14.dp))
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier.heightIn(max = 340.dp),
        ) {
            DialCountries.forEach { c ->
                DropdownMenuItem(
                    onClick = { onSelect(c.iso); open = false },
                    text = {
                        Text(
                            "${c.flag}  ${c.name}  +${c.dial}",
                            fontSize = 15.sp,
                            fontWeight = if (c.iso == iso) FontWeight.Bold else FontWeight.Medium,
                            color = if (c.iso == iso) VuAccentDeep else VuInk,
                            maxLines = 1,
                        )
                    },
                )
            }
        }
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
private fun ColorField(selected: String, dimmed: Boolean = false, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier.alpha(if (dimmed) 0.45f else 1f),
    ) {
        CardColors.forEach { c ->
            // Con foto de fondo el color no pinta nada, así que ninguna muestra sale marcada:
            // tocar una vuelve a activar el color y descarta la foto.
            val active = !dimmed && c.id == selected
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.tile)
                    .border(
                        if (active) 2.5.dp else 1.5.dp,
                        // El blanco se confunde con la tarjeta, así que lleva un borde de verdad.
                        if (active) c.ink else if (c.tile == Color.White) VuInk3 else VuLine,
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
                        // El check va del color del tile para contrastar con el círculo de tinta
                        // (en Negro la tinta ya es blanca).
                        Icon(VuelvoIcons.Check, contentDescription = c.label, tint = c.tile, modifier = Modifier.size(11.dp))
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
    reward: String,
) {
    val t = bizTypeById(type)
    val c = cardColorById(color)
    val logoBitmap = rememberDecoded(logo)
    val coverBitmap = rememberDecoded(cover)
    val hasCover = coverBitmap != null
    // Fondo oscuro: foto de portada o un tile oscuro (Negro). Ambos usan tinta blanca.
    val darkBg = hasCover || c.dark

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .then(
                    if (hasCover) Modifier.background(Color(0xFF0E0B16))
                    else Modifier.background(c.tile)
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
                                else Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (darkBg && !hasCover) Color.White.copy(alpha = 0.14f) else VuCard)
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
                            color = if (darkBg) Color.White else VuInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            t.label,
                            fontSize = 13.5.sp,
                            color = if (darkBg) Color.White.copy(alpha = 0.9f) else VuInk2,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                val stampSize = (26 - maxOf(0, stamps - 10) * 1.1).coerceIn(9.0, 26.0)
                if (darkBg) {
                    // frosted panel so the stamps stay legible over a dark background
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
                Spacer(Modifier.height(14.dp))
                // La recompensa cierra la tarjeta: es la promesa que el cliente ve al abrirla.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        VuelvoIcons.Tag,
                        contentDescription = null,
                        tint = if (darkBg) Color.White else c.ink,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        reward,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (darkBg) Color.White else VuInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Vista previa de la tarjeta del cliente", fontSize = 12.5.sp, color = VuInk3, fontWeight = FontWeight.SemiBold)
    }
}

// ── Shared buttons ───────────────────────────────────────────
@Composable
fun GradientButton(text: String, icon: ImageVector?, onClick: () -> Unit, enabled: Boolean = true) {
    val content = if (enabled) Color.White else VuInk3
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) AccentGradient else SolidColor(VuLine))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 17.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text, fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = content)
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
