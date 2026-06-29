package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.delta.vuelvo_commerce.billing.StoreManager
import com.delta.vuelvo_commerce.billing.SubscriptionPlan
import com.delta.vuelvo_commerce.billing.firstFormattedPrice
import com.delta.vuelvo_commerce.data.SubscriptionRepository
import com.delta.vuelvo_commerce.nfc.NfcTagWriter
import com.delta.vuelvo_commerce.nfc.WriteResult
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk3
import com.delta.vuelvo_commerce.ui.theme.VuLine
import androidx.activity.ComponentActivity

// TEMPORAL: con true, "Activar plan" simula la suscripción (sin Google Play Billing) para poder
// probar la escritura NFC. Poner en false / eliminar cuando el billing real esté operativo.
private const val DEV_SIMULATE_SUBSCRIPTION = true

@Composable
fun BizApp(
    store: StoreManager,
    deviceUuid: String,
    subscriptions: SubscriptionRepository,
) {
    val app: AppState = viewModel()
    val activity = LocalContext.current as ComponentActivity
    val nfcWriter = remember { NfcTagWriter(activity) }

    val storeSubscribed by store.isSubscribed.collectAsStateWithLifecycle()
    val storeActivePlan by store.activePlanId.collectAsStateWithLifecycle()
    val products by store.products.collectAsStateWithLifecycle()

    // La suscripción simulada (modo dev) cuenta como activa para desbloquear la escritura.
    val subscribed = storeSubscribed || (DEV_SIMULATE_SUBSCRIPTION && app.simulatedPlanId != null)
    val activePlan = storeActivePlan ?: app.simulatedPlanId.takeIf { DEV_SIMULATE_SUBSCRIPTION }

    // planId -> formatted Play price, used by the paywall instead of hardcoded prices.
    val prices = products.mapNotNull { details ->
        val planId = SubscriptionPlan.fromProductId(details.productId)?.planId
        val price = details.firstFormattedPrice
        if (planId != null && price != null) planId to price else null
    }.toMap()

    // Toast + jump to config when a subscription becomes active.
    var wasSubscribed by remember { mutableStateOf(subscribed) }
    LaunchedEffect(subscribed) {
        if (subscribed && !wasSubscribed) {
            app.showToast("Suscripción activada · ya puedes escribir tags")
            app.selectTab(BizTab.Config)
            // Registra la suscripción (uuid + fecha de finalización) en Firestore.
            SubscriptionPlan.fromPlanId(activePlan)?.let { plan ->
                runCatching { subscriptions.recordSubscription(deviceUuid, plan.endDateFromNow()) }
                    .onFailure { Log.w("BizApp", "No se pudo registrar la suscripción en Firestore", it) }
            }
        }
        wasSubscribed = subscribed
    }

    // Drive the NFC write session while the overlay is open.
    var writePhase by remember { mutableStateOf<WritePhase>(WritePhase.Writing) }
    LaunchedEffect(app.isWriting) {
        if (app.isWriting) {
            writePhase = WritePhase.Writing
            nfcWriter.writeSession(app.tagConfig.deeplinkUrl(deviceUuid)).collect { result ->
                writePhase = when (result) {
                    WriteResult.Success -> WritePhase.Success
                    WriteResult.Cancelled -> { app.stopWriting(); WritePhase.Writing }
                    is WriteResult.Failure -> WritePhase.Error(result.message)
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(VuBg),
    ) {
        // scrolling content
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            when (app.activeTab) {
                BizTab.Config -> ConfigScreen(
                    form = app.tagConfig,
                    onForm = app::updateForm,
                    subscribed = subscribed,
                    onWrite = { app.startWriting() },
                    onGoPaywall = { app.selectTab(BizTab.Paywall) },
                )

                BizTab.Paywall -> PaywallScreen(
                    subscribed = subscribed,
                    activePlan = activePlan,
                    onActivate = { planId ->
                        if (DEV_SIMULATE_SUBSCRIPTION) {
                            app.simulateSubscription(planId)
                        } else {
                            SubscriptionPlan.fromPlanId(planId)?.let { store.purchase(activity, it) }
                        }
                    },
                    prices = prices,
                )
            }
        }

        BizTabBar(
            tab = app.activeTab,
            onTab = { app.selectTab(it) },
            subscribed = subscribed,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // toast
        AnimatedVisibility(
            visible = app.toastMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp),
        ) {
            Toast(app.toastMessage ?: "")
        }

        if (app.isWriting) {
            WriteOverlay(form = app.tagConfig, phase = writePhase, onClose = { app.stopWriting() })
        }
    }
}

@Composable
private fun BizTabBar(
    tab: BizTab,
    onTab: (BizTab) -> Unit,
    subscribed: Boolean,
    modifier: Modifier = Modifier,
) {
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.92f))) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(VuLine))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .padding(bottom = 12.dp + navBottom)
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TabItem(
            label = "Escribir tag",
            icon = VuelvoIcons.Tag,
            active = tab == BizTab.Config,
            locked = !subscribed,
            modifier = Modifier.weight(1f),
        ) { onTab(BizTab.Config) }
        TabItem(
            label = if (subscribed) "Suscripción" else "Planes",
            icon = VuelvoIcons.Crown,
            active = tab == BizTab.Paywall,
            locked = false,
            modifier = Modifier.weight(1f),
            ) { onTab(BizTab.Paywall) }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) VuAccentDeep else VuInk3,
                modifier = Modifier.size(26.dp),
            )
            if (locked) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp)
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(VuInk),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(VuelvoIcons.Lock, null, tint = Color.White, modifier = Modifier.size(9.dp))
                }
            }
        }
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (active) VuAccentDeep else VuInk3,
            letterSpacing = (-0.1).sp,
        )
    }
}

@Composable
private fun Toast(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VuInk)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(Brush.linearGradient(listOf(VuAccent, VuAccentDeep))),
            contentAlignment = Alignment.Center,
        ) { Icon(VuelvoIcons.Check, null, tint = Color.White, modifier = Modifier.size(15.dp)) }
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
