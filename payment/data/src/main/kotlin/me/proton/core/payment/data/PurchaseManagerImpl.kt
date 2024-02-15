/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.payment.data

import kotlinx.coroutines.flow.Flow
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import javax.inject.Inject

@ExcludeFromCoverage
public class PurchaseManagerImpl @Inject constructor(
    private val repository: PurchaseRepository
) : PurchaseManager {
    override suspend fun addPurchase(purchase: Purchase): Unit =
        repository.upsertPurchase(purchase)

    override suspend fun getPurchase(planName: String): Purchase? =
        repository.getPurchase(planName)

    override suspend fun getPurchases(): List<Purchase> =
        repository.getPurchases()

    override fun observePurchase(planName: String): Flow<Purchase?> =
        repository.observePurchase(planName)

    override fun observePurchases(): Flow<List<Purchase>> =
        repository.observePurchases()

    override fun onPurchaseStateChanged(initialState: Boolean): Flow<Purchase> =
        repository.onPurchaseStateChanged(initialState)
}
