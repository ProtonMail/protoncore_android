/*
 * Copyright (c) 2025 Proton AG
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

/**
 * Mirrors the BillingResponseCode models from the Google BillingClient. The BillingResponseCode
 * properties are not available nor accessible in the payment modules.
 */
public object GoogleBillingResponseCode {

    public const val BILLING_UNAVAILABLE: Int = 3
    public const val DEVELOPER_ERROR: Int = 5
    public const val ERROR: Int = 6
    public const val FEATURE_NOT_SUPPORTED: Int = -2
    public const val ITEM_ALREADY_OWNED: Int = 7
    public const val ITEM_NOT_OWNED: Int = 8
    public const val ITEM_UNAVAILABLE: Int = 4
    public const val NETWORK_ERROR: Int = 12
    public const val OK: Int = 0
    public const val SERVICE_DISCONNECTED: Int = -1
    public const val SERVICE_UNAVAILABLE: Int = 2
    public const val USER_CANCELED: Int = 1
}
