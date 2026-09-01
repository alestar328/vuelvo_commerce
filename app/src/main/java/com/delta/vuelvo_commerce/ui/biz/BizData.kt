package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.delta.vuelvo_commerce.ui.VuelvoIcons

/** Establishment type — mirrors BIZ_TYPES in vuelvo-biz-config.jsx. */
data class BizType(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tile: Color,
    val ink: Color,
    val reward: String,
    /** SF Symbol que renderiza la app de consumidor (param `sym` del deeplink). */
    val sym: String,
)

val BizTypes = listOf(
    BizType("cafe", "Cafetería", VuelvoIcons.Coffee, Color(0xFFF3E9DF), Color(0xFF9A6A43), "Café gratis", "cup.and.saucer.fill"),
    BizType("forn", "Panadería", VuelvoIcons.Bread, Color(0xFFF6EEDC), Color(0xFFB8862B), "Producto gratis", "takeoutbag.and.cup.and.straw.fill"),
    BizType("pelu", "Peluquería", VuelvoIcons.Scissors, Color(0xFFE9EDF1), Color(0xFF5C6B7B), "Corte gratis", "scissors"),
    BizType("rest", "Restaurante", VuelvoIcons.Fork, Color(0xFFF6E7E1), Color(0xFFBC5A40), "Postre gratis", "fork.knife"),
    BizType("gelat", "Heladería", VuelvoIcons.IceCream, Color(0xFFF8E6EE), Color(0xFFCD5B8C), "Helado gratis", "birthday.cake.fill"),
    BizType("botiga", "Tienda", VuelvoIcons.Store, Color(0xFFE7F0EC), Color(0xFF3F8466), "Descuento", "bag.fill"),
)

fun bizTypeById(id: String): BizType = BizTypes.firstOrNull { it.id == id } ?: BizTypes[0]

/**
 * Card colour palette — mirrors CARD_COLORS in vuelvo-biz-config.jsx. The merchant picks one and it
 * tints the customer's card. [id] is the stable cross-platform token written to the NFC tag so iOS
 * and Android resolve to the same swatch; [tile] is the card background, [ink] the icon colour.
 * [dark] marks tiles too dark for the default ink, so the card flips to white text (same treatment
 * the cover photo already uses).
 */
data class CardColor(
    val id: String,
    val label: String,
    val tile: Color,
    val ink: Color,
    val dark: Boolean = false,
)

val CardColors = listOf(
    CardColor("cafe", "Arena", Color(0xFFE8CFAF), Color(0xFF9A6A43)),
    CardColor("amber", "Ámbar", Color(0xFFF0D389), Color(0xFFB8862B)),
    CardColor("rose", "Rosa", Color(0xFFF3C2D6), Color(0xFFCD5B8C)),
    CardColor("coral", "Coral", Color(0xFFF5C2B0), Color(0xFFBC5A40)),
    CardColor("mint", "Menta", Color(0xFFB8DFCB), Color(0xFF3F8466)),
    CardColor("sky", "Cielo", Color(0xFFBCD4E6), Color(0xFF5C6B7B)),
    CardColor("violet", "Violeta", Color(0xFFD5C2F7), Color(0xFF7B3CE6)),
    CardColor("ink", "Carbón", Color(0xFFC7C3D4), Color(0xFF3A3550)),
    CardColor("white", "Blanco", Color(0xFFFFFFFF), Color(0xFF3A3550)),
    CardColor("black", "Negro", Color(0xFF000000), Color(0xFFFFFFFF), dark = true),
)

fun cardColorById(id: String): CardColor = CardColors.firstOrNull { it.id == id } ?: CardColors[0]

/** Subscription plan — mirrors BIZ_PLANS in vuelvo-biz-paywall.jsx. */
data class BizPlan(
    val id: String,
    val name: String,
    val price: String,
    val unit: String,
    val note: String,
    val sub: String? = null,
    val badge: String? = null,
)

val BizPlans = listOf(
    BizPlan("monthly", "Mensual", "9,99 €", "/mes", "Facturado cada mes"),
    BizPlan("quarterly", "Trimestral", "24,99 €", "/3 meses", "8,33 €/mes · facturado cada 3 meses", sub = "Ahorra 17%"),
    BizPlan("annual", "Anual", "79,99 €", "/año", "6,67 €/mes · facturado cada año", sub = "Ahorra 33%", badge = "Mejor valor"),
)

fun bizPlanById(id: String?): BizPlan = BizPlans.firstOrNull { it.id == id } ?: BizPlans[0]

val BizPerks = listOf(
    "Escribe tags NFC ilimitados",
    "Tarjetas con tu marca y recompensas",
    "Estadísticas de clientes recurrentes",
    "Cancela cuando quieras",
)

/**
 * Form state for the tag-configuration screen.
 *
 * [logo] and [cover] hold the merchant's images already encoded as compact Base64 strings (see
 * [com.delta.vuelvo_commerce.ui.biz.TagImageCodec]); storing the encoded string — not a Uri — keeps
 * a single source of truth shared by the live preview, the write overlay and the NFC payload.
 * [color] is a [CardColor] id from the shared palette.
 */
data class TagForm(
    val title: String = "",
    val type: String = "cafe",
    val stamps: Int = 10,
    /** Business logo, shown in the card avatar. Base64 (url-safe, no padding) JPEG, or null. */
    val logo: String? = null,
    /** Full-bleed card background. Base64 (url-safe, no padding) JPEG, or null. */
    val cover: String? = null,
    /** Selected card colour — a [CardColors] id (default mirrors the prototype). */
    val color: String = "cafe",
)
