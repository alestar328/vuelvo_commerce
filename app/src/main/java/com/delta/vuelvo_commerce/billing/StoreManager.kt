package com.delta.vuelvo_commerce.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wraps Google Play Billing: loads the three subscription products, tracks whether the merchant has
 * an active subscription, and launches the purchase flow. All state is exposed as [StateFlow] for
 * the Compose UI to observe.
 */
class StoreManager(context: Context) {

    private val appContext = context.applicationContext

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    /** Plan id ([SubscriptionPlan.planId]) of the active subscription, or null. */
    private val _activePlanId = MutableStateFlow<String?>(null)
    val activePlanId: StateFlow<String?> = _activePlanId.asStateFlow()

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { acknowledgeIfNeeded(it) }
            refreshSubscriptionState()
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    /** Connects to Play Billing and loads products + current subscription state. Safe to call repeatedly. */
    fun connect() {
        if (billingClient.isReady) {
            queryProducts()
            refreshSubscriptionState()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    refreshSubscriptionState()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Left to the next connect() call to retry.
            }
        })
    }

    fun disconnect() {
        billingClient.endConnection()
    }

    private fun queryProducts() {
        val productList = SubscriptionPlan.entries.map { plan ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(plan.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = details
            }
        }
    }

    private fun refreshSubscriptionState() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync

            val active = purchases.firstOrNull {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            _isSubscribed.value = active != null
            _activePlanId.value = active
                ?.products
                ?.firstOrNull()
                ?.let { SubscriptionPlan.fromProductId(it)?.planId }

            purchases.forEach { acknowledgeIfNeeded(it) }
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { }
        }
    }

    /** Launches the Play purchase flow for [plan]. No-op if the product hasn't loaded yet. */
    fun purchase(activity: Activity, plan: SubscriptionPlan) {
        val details = _products.value.firstOrNull { it.productId == plan.productId } ?: return
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build(),
                ),
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }
}

/** First-phase formatted price of a subscription product (e.g. "9,99 €"), or null. */
val ProductDetails.firstFormattedPrice: String?
    get() = subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.formattedPrice
