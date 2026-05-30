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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delta.vuelvo_commerce.ui.VuelvoIcons
import com.delta.vuelvo_commerce.ui.theme.VuAccent
import com.delta.vuelvo_commerce.ui.theme.VuAccentDeep
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.VuInk
import com.delta.vuelvo_commerce.ui.theme.VuInk3
import com.delta.vuelvo_commerce.ui.theme.VuLine
import kotlinx.coroutines.delay

private enum class BizTab { Config, Paywall }

@Composable
fun BizApp(startSubscribed: Boolean = false) {
    var tab by remember { mutableStateOf(BizTab.Config) }
    var subscribed by remember { mutableStateOf(startSubscribed) }
    var activePlan by remember { mutableStateOf<String?>(if (startSubscribed) "annual" else null) }
    var writing by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(TagForm()) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3200)
            toast = null
        }
    }

    val activate: (String) -> Unit = { planId ->
        subscribed = true
        activePlan = planId
        toast = "Plan ${bizPlanById(planId).name} activado · ya puedes escribir tags"
        tab = BizTab.Config
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
            when (tab) {
                BizTab.Config -> ConfigScreen(
                    form = form,
                    onForm = { form = it },
                    subscribed = subscribed,
                    onWrite = { writing = true },
                    onGoPaywall = { tab = BizTab.Paywall },
                )

                BizTab.Paywall -> PaywallScreen(
                    subscribed = subscribed,
                    activePlan = activePlan,
                    onActivate = activate,
                )
            }
        }

        BizTabBar(
            tab = tab,
            onTab = { tab = it },
            subscribed = subscribed,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // toast
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp),
        ) {
            Toast(toast ?: "")
        }

        if (writing) {
            WriteOverlay(form = form, onClose = { writing = false })
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
