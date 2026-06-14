package com.delta.vuelvo_commerce.ui.biz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The two tabs of the merchant shell. */
enum class BizTab { Config, Paywall }

/**
 * Global, in-memory app state. Survives recompositions and configuration changes (it is a
 * [ViewModel]) but not process death / reinstalls — matching the iOS behaviour.
 *
 * Subscription state is intentionally *not* held here; it is owned by
 * [com.delta.vuelvo_commerce.billing.StoreManager] and observed by the UI.
 */
class AppState : ViewModel() {

    var activeTab by mutableStateOf(BizTab.Config)
        private set

    /** Live form state shown in the config screen (the editable "TagConfig"). */
    var tagConfig by mutableStateOf(TagForm())
        private set

    /** Whether the NFC write overlay is showing. */
    var isWriting by mutableStateOf(false)
        private set

    /** Auto-dismissing toast message, or null. */
    var toastMessage by mutableStateOf<String?>(null)
        private set

    // ──────────────────────────────────────────────────────────────────────────
    // TEMPORAL: simulación de suscripción para poder probar la escritura NFC sin
    // pasar por Google Play Billing. Quitar (junto con DEV_SIMULATE_SUBSCRIPTION)
    // cuando el billing real esté operativo.
    /**
     * Plan id simulado como activo, o null.
     * TEMPORAL: arranca con "annual" para que la suscripción esté activa desde el inicio y se
     * pueda probar la escritura NFC sin pulsar "Activar plan". Poner a null cuando el billing
     * real esté operativo.
     */
    var simulatedPlanId by mutableStateOf<String?>("annual")
        private set

    fun simulateSubscription(planId: String) {
        simulatedPlanId = planId
    }
    // ──────────────────────────────────────────────────────────────────────────

    private var toastJob: Job? = null

    fun selectTab(tab: BizTab) {
        activeTab = tab
    }

    fun updateForm(form: TagForm) {
        tagConfig = form
    }

    fun startWriting() {
        isWriting = true
    }

    fun stopWriting() {
        isWriting = false
    }

    fun showToast(message: String, durationMs: Long = 3000) {
        toastJob?.cancel()
        toastMessage = message
        toastJob = viewModelScope.launch {
            delay(durationMs)
            toastMessage = null
        }
    }
}
