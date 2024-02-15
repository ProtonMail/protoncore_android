package me.proton.core.payment.domain.extension

import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage
public suspend fun GoogleBillingRepository<*>.findGooglePurchase(
    purchase: Purchase
): GooglePurchase? = querySubscriptionPurchases().let { subscriptions ->
    subscriptions.firstOrNull { it.orderId == purchase.paymentOrderId }
}
