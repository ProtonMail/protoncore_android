/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.payment.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.entity.toPurchaseEntity
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.repository.PurchaseRepository
import javax.inject.Inject

public class PurchaseRepositoryImpl @Inject constructor(
    db: PaymentDatabase
) : PurchaseRepository {

    private val purchaseDao = db.purchaseDao()

    // Accept 10 nested/concurrent state changes -> extraBufferCapacity.
    private val purchaseStateChanged = MutableSharedFlow<Purchase>(extraBufferCapacity = 10)

    private fun tryEmitPurchaseStateChanged(purchase: Purchase) {
        if (!purchaseStateChanged.tryEmit(purchase))
            throw IllegalStateException("Too many nested account state changes, extra buffer capacity exceeded.")
    }

    private suspend fun updatePurchaseState(planName: String, state: PurchaseState) {
        purchaseDao.updatePurchaseState(planName, state)
        getPurchase(planName)?.let { tryEmitPurchaseStateChanged(it) }
    }

    override fun observePurchase(planName: String): Flow<Purchase?> =
        purchaseDao.observe(planName).map { it?.toPurchase() }

    override fun observePurchases(): Flow<List<Purchase>> =
        purchaseDao.observe().map { list -> list.map { it.toPurchase() } }

    override suspend fun getPurchase(planName: String): Purchase? =
        purchaseDao.get(planName)?.toPurchase()

    override suspend fun getPurchases(): List<Purchase> =
        purchaseDao.getAll().map { it.toPurchase() }

    override suspend fun upsertPurchase(purchase: Purchase): Unit =
        purchaseDao.insertOrUpdate(purchase.toPurchaseEntity())
            .also { updatePurchaseState(purchase.planName, purchase.purchaseState) }

    override suspend fun deletePurchase(planName: String): Unit =
        updatePurchaseState(planName, PurchaseState.Deleted)
            .also { purchaseDao.delete(planName) }

    override fun onPurchaseStateChanged(initialState: Boolean): Flow<Purchase> =
        purchaseStateChanged.asSharedFlow()
            .onSubscription { if (initialState) getPurchases().forEach { emit(it) } }
            .distinctUntilChanged()
}
