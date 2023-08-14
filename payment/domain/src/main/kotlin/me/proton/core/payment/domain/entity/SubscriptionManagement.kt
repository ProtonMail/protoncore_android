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

package me.proton.core.payment.domain.entity

import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotal

public enum class SubscriptionManagement(public val value: Int) {
    PROTON_MANAGED(0),
    APPLE_MANAGED(1),
    GOOGLE_MANAGED(2);

    public companion object {
        public val map: Map<Int, SubscriptionManagement> = values().associateBy { it.value }
    }
}

public fun SubscriptionManagement.toCheckoutBillingSubscribeManager(): CheckoutBillingSubscribeTotal.Manager {
    return when (this) {
        SubscriptionManagement.PROTON_MANAGED -> CheckoutBillingSubscribeTotal.Manager.proton
        SubscriptionManagement.GOOGLE_MANAGED -> CheckoutBillingSubscribeTotal.Manager.google
        SubscriptionManagement.APPLE_MANAGED -> error("Cannot checkout Apple subscription.")
    }
}
