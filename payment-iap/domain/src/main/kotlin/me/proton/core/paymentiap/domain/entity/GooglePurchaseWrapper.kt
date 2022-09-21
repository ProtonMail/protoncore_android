/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.paymentiap.domain.entity

import com.android.billingclient.api.Purchase
import me.proton.core.payment.domain.entity.GooglePurchase

internal data class GooglePurchaseWrapper(val purchase: Purchase) : GooglePurchase {
    override val orderId: String get() = purchase.orderId
    override val packageName: String get() = purchase.packageName
    override val productIds: List<String> get() = purchase.products
    override val purchaseToken: String get() = purchase.purchaseToken
}

public fun GooglePurchase.unwrap(): Purchase = (this as GooglePurchaseWrapper).purchase

public fun Purchase.wrap(): GooglePurchase = GooglePurchaseWrapper(this)
