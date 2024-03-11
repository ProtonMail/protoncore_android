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

package me.proton.core.test.android.mocks

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import io.mockk.every
import io.mockk.mockk
import me.proton.core.paymentiap.domain.BillingClientFactory

class FakeBillingClientFactory : BillingClientFactory {
    private val _listeners = mutableListOf<PurchasesUpdatedListener>()

    val billingClient: BillingClient = mockk()
    val listeners: List<PurchasesUpdatedListener> = _listeners

    override fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): BillingClient {
        _listeners.add(purchasesUpdatedListener)
        every { billingClient.endConnection() } answers {
            _listeners.remove(purchasesUpdatedListener)
        }
        return billingClient
    }
}

fun FakeBillingClientFactory.mockBillingClientSuccess() {
    billingClient.mockBillingClientSuccess { listeners }
}
