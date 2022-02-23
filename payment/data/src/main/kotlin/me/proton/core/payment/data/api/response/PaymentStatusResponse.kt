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

package me.proton.core.payment.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.PaymentStatus

@Serializable
internal data class PaymentStatusResponse(
    @SerialName("Card")
    val card: Boolean,
    @SerialName("Paypal")
    val paypal: Boolean,
    @SerialName("Apple")
    val apple: Boolean,
    @SerialName("Bitcoin")
    val bitcoin: Boolean,
    @SerialName("Stripe")
    val stripe: Boolean,
    @SerialName("Paymentwall")
    val paymentWall: Boolean,
    @SerialName("Blockchain.info")
    val blockchainInfo: Boolean
) {
    fun toPaymentStatus(): PaymentStatus = PaymentStatus(
        card = card,
        paypal = paypal,
        apple = apple,
        bitcoin = bitcoin,
        stripe = stripe,
        paymentWall = paymentWall,
        blockchainInfo = blockchainInfo
    )
}
