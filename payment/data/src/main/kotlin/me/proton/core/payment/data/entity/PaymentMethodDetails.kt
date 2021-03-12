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

@Serializable
internal data class PaymentMethodDetails(
    @SerialName("ExpMonth")
    val expirationMonth: String? = null,
    @SerialName("ExpYear")
    val expirationYear: String? = null,
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Country")
    val country: String? = null,
    @SerialName("ZIP")
    val zip: String? = null,
    @SerialName("Brand")
    val brand: String? = null,
    @SerialName("Last4")
    val last4: String? = null,
    @SerialName("ThreeDSSupport")
    val threeDS: Boolean? = null,
    @SerialName("BillingAgreementID")
    val billingAgreementID: String? = null,
    @SerialName("Payer")
    val payer: String? = null
)
