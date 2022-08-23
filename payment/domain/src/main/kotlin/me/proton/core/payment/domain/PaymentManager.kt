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

package me.proton.core.payment.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.PaymentProvider

public interface PaymentManager {

    /**
     * Returns a set of [PaymentProvider] which can be offered to the user.
     *
     * If [userId] is null the primary [userId] will be used.
     *
     * Note: In case it's not possible to define the status, an empty set is returned.
     */
    public suspend fun getPaymentProviders(userId: UserId? = null, refresh: Boolean = false): Set<PaymentProvider>

    /**
     * Returns true if there is an upgrade flow available.
     *
     * If [userId] is null the primary [userId] will be used.
     *
     * Note: In case it's not possible to define the status, false is returned.
     */
    public suspend fun isUpgradeAvailable(userId: UserId? = null, refresh: Boolean = false): Boolean

    /**
     * Returns true if there is (current) subscription flow available.
     *
     * Note: In case it's not possible to define the status, false is returned.
     */
    public suspend fun isSubscriptionAvailable(userId: UserId, refresh: Boolean = false): Boolean
}
