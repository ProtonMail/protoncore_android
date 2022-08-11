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

package me.proton.core.paymentcommon.domain.entity

public data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val details: Details?
)

public sealed class Details {
    public data class CardDetails(val cardDetails: Card.CardReadOnly) : Details()
    public data class PayPalDetails(
        val billingAgreementId: String,
        val payer: String
    ) : Details()
}

public enum class PaymentMethodType(public val value: String) {
    CARD("card"),
    PAYPAL("paypal");
    // TODO add google iap option here

    public companion object {
        public val map: Map<String, PaymentMethodType> = values().associateBy { it.value }
    }
}
