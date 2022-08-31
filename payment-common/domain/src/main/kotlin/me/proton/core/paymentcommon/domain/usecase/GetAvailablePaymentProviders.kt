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

package me.proton.core.paymentcommon.domain.usecase

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import javax.inject.Inject

public class GetAvailablePaymentProviders @Inject internal constructor(
    private val accountManager: AccountManager,
    private val appStore: AppStore,
    private val getPaymentStatus: GetPaymentStatus,
    private val protonIAPBillingLibrary: ProtonIAPBillingLibrary
) {
    /**
     * Returns a set of [payment providers][PaymentProvider] which can be offered to the user.
     *
     * If [userId] is null the primary [userId] will be used.
     *
     * Note: In case it's not possible to fetch current payment status, an empty set is returned.
     */
    public suspend operator fun invoke(userId: UserId? = null, refresh: Boolean = false): Set<PaymentProvider> {
        val paymentStatus = try {
            getPaymentStatus(userId ?: accountManager.getPrimaryUserId().first(), refresh)
        } catch (_: ApiException) {
            null
        }
        return buildSet {
            if (paymentStatus?.card == true) add(PaymentProvider.CardPayment)
            if (paymentStatus?.paypal == true) add(PaymentProvider.PayPal)
            if (paymentStatus?.inApp == true && isBuiltForGooglePlay()) add(PaymentProvider.GoogleInAppPurchase)
        }
    }

    private fun isBuiltForGooglePlay(): Boolean =
        appStore == AppStore.GooglePlay && protonIAPBillingLibrary.isAvailable()
}

public enum class PaymentProvider {
    CardPayment,
    GoogleInAppPurchase,
    PayPal
}
