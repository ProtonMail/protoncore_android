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

package me.proton.core.paymentcommon.data

import me.proton.core.domain.entity.UserId
import me.proton.core.paymentcommon.domain.PaymentManager
import me.proton.core.paymentcommon.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.paymentcommon.domain.usecase.PaymentProvider
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.canReadSubscription
import javax.inject.Inject

public class PaymentManagerImpl @Inject constructor(
    private val userManager: UserManager,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders
) : PaymentManager {

    override suspend fun getPaymentProviders(userId: UserId?, refresh: Boolean): Set<PaymentProvider> =
        getAvailablePaymentProviders.invoke(userId, refresh)

    override suspend fun isUpgradeAvailable(userId: UserId?, refresh: Boolean): Boolean =
        getAvailablePaymentProviders.invoke(userId, refresh).isNotEmpty()

    override suspend fun isSubscriptionAvailable(userId: UserId, refresh: Boolean): Boolean =
        userManager.getUser(userId).canReadSubscription()
}
