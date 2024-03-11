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

package me.proton.core.paymentiap.presentation.entity

import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.Purchase
import io.mockk.every
import io.mockk.mockk

internal fun mockPurchase(
    accountIdentifiers: AccountIdentifiers? = null,
    orderId: String = "orderId",
    isAcknowledged: Boolean = false,
    purchaseToken: String = "token",
    purchaseTime: Long = 0,
    purchaseState: Int = Purchase.PurchaseState.PURCHASED,
    products: List<String> = listOf("product1"),
    packageName: String = "packageName"
): Purchase = mockk<Purchase> {
    every { this@mockk.accountIdentifiers } returns accountIdentifiers
    every { this@mockk.orderId } returns orderId
    every { this@mockk.packageName } returns packageName
    every { this@mockk.purchaseState } returns purchaseState
    every { this@mockk.purchaseTime } returns purchaseTime
    every { this@mockk.purchaseToken } returns purchaseToken
    every { this@mockk.products } returns products
    every { this@mockk.isAcknowledged } returns isAcknowledged
}
