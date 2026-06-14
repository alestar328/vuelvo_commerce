package com.delta.vuelvo_commerce.billing

/**
 * The three subscription tiers, linking each Play Billing product ID with the plan id used by the
 * UI ([com.delta.vuelvo_commerce.ui.biz.BizPlan.id]).
 */
enum class SubscriptionPlan(val planId: String, val productId: String) {
    MONTHLY("monthly", "com.bellon.vuelvocommerce.subscription.monthly"),
    QUARTERLY("quarterly", "com.bellon.vuelvocommerce.subscription.quarterly"),
    ANNUAL("annual", "com.bellon.vuelvocommerce.subscription.annual");

    companion object {
        fun fromProductId(productId: String): SubscriptionPlan? =
            entries.firstOrNull { it.productId == productId }

        fun fromPlanId(planId: String?): SubscriptionPlan? =
            entries.firstOrNull { it.planId == planId }
    }
}
