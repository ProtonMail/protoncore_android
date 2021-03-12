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
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Details
import me.proton.core.util.kotlin.exhaustive

@Serializable
internal data class PaymentMethod(
    @SerialName("ID")
    val id: String,
    @SerialName("Type")
    val type: String,
    @SerialName("Details")
    val paymentMethodDetails: PaymentMethodDetails
) {
    fun toDetails(): Details? {
        return paymentMethodDetails.let {
            when (type) {
                "card" -> {
                    Details.CardDetails(
                        Card.CardReadOnly(
                            it.brand!!,
                            it.last4!!,
                            it.expirationMonth!!,
                            it.expirationYear!!,
                            it.name!!,
                            it.country!!,
                            it.zip!!
                        )
                    )
                }
                "paypal" -> {
                    Details.PayPalDetails(it.billingAgreementID!!, it.payer!!)
                }
                else -> null
            }.exhaustive
        }
    }
}
