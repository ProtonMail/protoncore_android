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

package me.proton.core.paymentiap.data

import com.android.billingclient.api.BillingClient

internal val listOfKnownBillingCodes = listOf(
    BillingClient.BillingResponseCode.OK,
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
    BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
    BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
    BillingClient.BillingResponseCode.USER_CANCELED
)