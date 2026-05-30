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
)

val BizTypes = listOf(
    BizType("cafe", "Cafetería", VuelvoIcons.Coffee, Color(0xFFF3E9DF), Color(0xFF9A6A43), "Café gratis"),
    BizType("forn", "Panadería", VuelvoIcons.Bread, Color(0xFFF6EEDC), Color(0xFFB8862B), "Producto gratis"),
    BizType("pelu", "Peluquería", VuelvoIcons.Scissors, Color(0xFFE9EDF1), Color(0xFF5C6B7B), "Corte gratis"),
    BizType("rest", "Restaurante", VuelvoIcons.Fork, Color(0xFFF6E7E1), Color(0xFFBC5A40), "Postre gratis"),
    BizType("gelat", "Heladería", VuelvoIcons.IceCream, Color(0xFFF8E6EE), Color(0xFFCD5B8C), "Helado gratis"),
    BizType("botiga", "Tienda", VuelvoIcons.Store, Color(0xFFE7F0EC), Color(0xFF3F8466), "Descuento"),
)

fun bizTypeById(id: String): BizType = BizTypes.firstOrNull { it.id == id } ?: BizTypes[0]

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

/** Form state for the tag-configuration screen. */
data class TagForm(
    val title: String = "",
    val type: String = "cafe",
    val stamps: Int = 10,
)
