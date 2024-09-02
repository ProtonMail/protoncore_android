/*
 * Copyright (c) 2024. Proton AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.payment.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.util.kotlin.toBooleanOrFalse

@Serializable
internal data class PaymentStatusV5Response(
    @SerialName("VendorStates")
    val vendorStates: VendorStates
) {
    @Serializable
    data class VendorStates(
        @SerialName("Card")
        val card: Int?,
        @SerialName("InApp")
        val inApp: Int?,
        @SerialName("Paypal")
        val paypal: Int?
    )

    fun toPaymentStatus(): PaymentStatus = with(vendorStates) {
        PaymentStatus(
            card = card?.toBooleanOrFalse(),
            inApp = inApp?.toBooleanOrFalse(),
            paypal = paypal?.toBooleanOrFalse()
        )
    }
}
