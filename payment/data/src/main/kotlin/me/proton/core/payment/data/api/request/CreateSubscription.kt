/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.payment.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSubscription(
    @SerialName("Amount")
    val amount: Long,
    @SerialName("Currency")
    val currency: String,
    @SerialName("Payment")
    val payment: TokenTypePaymentBody? = null,
    @SerialName("Codes")
    val codes: List<String>? = null,
    @SerialName("Plans")
    val plans: Map<String, Int>,
    @SerialName("Cycle")
    val cycle: Int,
    @SerialName("External")
    val external: Int
)
