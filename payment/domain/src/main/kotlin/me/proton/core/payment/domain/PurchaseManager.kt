package me.proton.core.payment.domain

import kotlinx.coroutines.flow.Flow
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage
public interface PurchaseManager {
    public suspend fun addPurchase(purchase: Purchase)
    public suspend fun getPurchase(planName: String): Purchase?
    public suspend fun getPurchases(): List<Purchase>
    public fun observePurchase(planName: String): Flow<Purchase?>
    public fun observePurchases(): Flow<List<Purchase>>
    public fun onPurchaseStateChanged(initialState: Boolean = true): Flow<Purchase>
}
