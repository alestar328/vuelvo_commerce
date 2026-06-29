package com.delta.vuelvo_commerce.billing

import java.time.ZonedDateTime
import java.util.Date

/**
 * The three subscription tiers, linking each Play Billing product ID with the plan id used by the
 * UI ([com.delta.vuelvo_commerce.ui.biz.BizPlan.id]) and the billing period in months.
 */
enum class SubscriptionPlan(val planId: String, val productId: String, val durationMonths: Long) {
    MONTHLY("monthly", "com.bellon.vuelvocommerce.subscription.monthly", 1),
    QUARTERLY("quarterly", "com.bellon.vuelvocommerce.subscription.quarterly", 3),
    ANNUAL("annual", "com.bellon.vuelvocommerce.subscription.annual", 12);

    /**
     * Subscription end date computed as now + [durationMonths]. Play Billing does not expose the
     * server-side expiry on the client, so we derive it from the plan's period.
     */
    fun endDateFromNow(): Date =
        Date.from(ZonedDateTime.now().plusMonths(durationMonths).toInstant())

    companion object {
        fun fromProductId(productId: String): SubscriptionPlan? =
            entries.firstOrNull { it.productId == productId }

        fun fromPlanId(planId: String?): SubscriptionPlan? =
            entries.firstOrNull { it.planId == planId }
    }
}
