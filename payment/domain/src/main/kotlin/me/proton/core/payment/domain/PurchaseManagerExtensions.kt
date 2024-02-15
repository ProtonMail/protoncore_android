package me.proton.core.payment.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

/**
 * Flow of Purchase where [Purchase.purchaseState] equals [state].
 *
 * @param initialState if true (default), initial state for all purchases in this [state] will be raised on subscription.
 */
@ExcludeFromCoverage
public fun PurchaseManager.onPurchaseState(
    vararg state: PurchaseState,
    planName: String? = null,
    initialState: Boolean = true
): Flow<Purchase> = onPurchaseStateChanged(initialState)
    .filter { state.contains(it.purchaseState) }
    .filter { planName?.equals(it.planName) ?: true }
