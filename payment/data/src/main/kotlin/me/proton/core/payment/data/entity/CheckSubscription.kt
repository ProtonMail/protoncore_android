/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.payment.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Check Subscription Body.
 * @param codes this is an array of any coupon or gift codes.
 * @param planIds an array of PlanIds for which the user wants to make a new subscription. This is an array because
 * at the same moment a subscription could be made for multiple plans (ex. Mail Plus & VPN Plus etc..).
 * @param currency obviously a currency in which the payment will be made.
 * @param cycle this one is a cycle (currently supported cycle 1 or 12 meaning monthly or yearly).
 */
@Serializable
internal data class CheckSubscription(
    @SerialName("Codes")
    private var codes: List<String>? = null,
    @SerialName("PlanIDs")
    private val planIds: List<String>? = null,
    @SerialName("Currency")
    private val currency: String? = null,
    @SerialName("Cycle")
    private val cycle: Int
)
