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

package me.proton.core.paymentiap.domain

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import me.proton.core.payment.domain.repository.BillingClientError

public fun ProductDetails.firstPriceOrNull(): ProductDetails.PricingPhase? =
    subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)

public fun BillingClientError.isRetryable(): Boolean = when (responseCode) {
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> true
    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> true
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> true
    BillingClient.BillingResponseCode.ERROR -> true
    else -> false
}
