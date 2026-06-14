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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuAccentLine
import com.delta.vuelvo_commerce.ui.theme.VuAccentSoft
import com.delta.vuelvo_commerce.ui.theme.VuCard
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk3
import com.delta.vuelvo_commerce.ui.theme.VuLine
import com.delta.vuelvo_commerce.ui.theme.VuStampEmpty

@Composable
fun PaywallScreen(
    subscribed: Boolean,
    activePlan: String?,
    onActivate: (String) -> Unit,
    prices: Map<String, String> = emptyMap(),
) {
    if (subscribed) {
        ActiveSubscription(activePlan)
    } else {
        PaywallOffer(onActivate, prices)
    }
}

@Composable
private fun ActiveSubscription(activePlan: String?) {
    val p = bizPlanById(activePlan)
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 130.dp)) {
        BizHeader(title = "Suscripción", subtitle = "Tu plan está activo.")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(AccentGradient)
                .padding(horizontal = 22.dp, vertical = 26.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(VuelvoIcons.Crown, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                Column {
                    Text(
                        "Plan ${p.name}".uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp,
                    )
                    Text("Escritura activa", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.4).sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                BizPerks.forEach { perk ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(VuelvoIcons.Check, null, tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(18.dp))
                        Text(perk, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "${p.note} · Se renueva automáticamente",
            Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            color = VuInk3,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PaywallOffer(onActivate: (String) -> Unit, prices: Map<String, String>) {
    var sel by remember { mutableStateOf("annual") }
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 150.dp)) {
        BizHeader(title = "Desbloquea la escritura")

        // hero
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(VuAccentSoft, Color.Transparent)))
                .border(1.dp, VuAccentLine, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(50.dp).clip(RoundedCornerShape(15.dp)).background(AccentGradient),
                    contentAlignment = Alignment.Center,
                ) { Icon(VuelvoIcons.Crown, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
                Column {
                    Text("Vuelvo para comercios", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = VuInk, letterSpacing = (-0.4).sp)
                    Text("Fideliza a tus clientes", fontSize = 13.5.sp, color = com.delta.vuelvo_commerce.ui.theme.VuInk2, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BizPerks.forEach { perk ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape).background(VuAccentSoft),
                            contentAlignment = Alignment.Center,
                        ) { Icon(VuelvoIcons.Check, null, tint = VuAccentDeep, modifier = Modifier.size(14.dp)) }
                        Text(perk, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = VuInk)
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))

        // plans
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            BizPlans.forEach { p ->
                PlanRow(p, prices[p.id] ?: p.price, sel == p.id) { sel = p.id }
            }
        }
        Spacer(Modifier.height(22.dp))

        GradientButton(
            text = "Activar plan ${bizPlanById(sel).name.lowercase()}",
            icon = null,
            onClick = { onActivate(sel) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Renovación automática. Puedes cancelar en cualquier momento desde los ajustes de tu cuenta.",
            Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = VuInk3,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PlanRow(plan: BizPlan, priceText: String, active: Boolean, onSelect: () -> Unit) {
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(VuCard)
                .border(
                    if (active) 2.dp else 1.5.dp,
                    if (active) com.delta.vuelvo_commerce.ui.theme.VuAccent else VuLine,
                    RoundedCornerShape(20.dp),
                )
                .clickable(onClick = onSelect)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            // radio
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(if (active) Modifier.background(AccentGradient) else Modifier.border(2.dp, VuStampEmpty, CircleShape)),
                contentAlignment = Alignment.Center,
            ) {
                if (active) Icon(VuelvoIcons.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.name, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = VuInk, letterSpacing = (-0.3).sp)
                    if (plan.sub != null) {
                        Box(
                            Modifier.clip(CircleShape).background(VuAccentSoft).padding(horizontal = 8.dp, vertical = 3.dp),
                        ) { Text(plan.sub, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = VuAccentDeep) }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(plan.note, fontSize = 12.5.sp, color = VuInk3, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(priceText, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = VuInk, letterSpacing = (-0.4).sp)
                Text(plan.unit, fontSize = 12.sp, color = VuInk3, fontWeight = FontWeight.SemiBold)
            }
        }
        // "Mejor valor" badge
        if (plan.badge != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .offset(y = (-11).dp)
                    .clip(CircleShape)
                    .background(AccentGradient)
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) { Text(plan.badge, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.2.sp) }
        }
    }
}
