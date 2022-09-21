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

package me.proton.core.payment.domain.entity

public sealed class PaymentTokenResult(
    public open val status: PaymentTokenStatus
) {
    public data class CreatePaymentTokenResult constructor(
        override val status: PaymentTokenStatus,
        val approvalUrl: String?,
        val token: ProtonPaymentToken,
        val returnHost: String?
    ) : PaymentTokenResult(status)

    public data class PaymentTokenStatusResult(
        override val status: PaymentTokenStatus
    ) : PaymentTokenResult(status)
}

public enum class PaymentTokenStatus(internal val id: Int) {

    /** Payment requires additional verification. */
    PENDING(0),

    /** Token can be charged immediately. */
    CHARGEABLE(1),

    /** Additional verification of the token failed, cannot be used for payment. */
    FAILED(2),

    /**
     * Token has been consumed in a transaction and cannot be reused anymore, it can however be stored as a payment
     * method at which point it converts to a permanently reusable payment method.
     */
    CONSUMED(3),

    /**
     * Requested verification of the token is currently not supported, payment method cannot be used.
     */
    NOT_SUPPORTED(4);

    public companion object {
        public val map: Map<Int, PaymentTokenStatus> = values().associateBy { it.id }
    }
}
