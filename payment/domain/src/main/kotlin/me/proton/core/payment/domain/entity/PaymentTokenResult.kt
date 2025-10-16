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

    /**
     * The [ProtonPaymentToken] has yet to be approved or denied, check the status again.
     */
    PENDING(0),

    /**
     * The [ProtonPaymentToken] is now approved, chargeable, and ready for consumption.
     */
    CHARGEABLE(1),

    /**
     * The [ProtonPaymentToken] has not been approved, and therefore cannot be consumed.
     */
    FAILED(2),

    /**
     * The [ProtonPaymentToken] has already been consumed.
     */
    CONSUMED(3),

    /**
     * The [ProtonPaymentToken] failed approval, due to a specific unsupported reason..
     */
    NOT_SUPPORTED(4);

    public companion object {
        public val map: Map<Int, PaymentTokenStatus> = values().associateBy { it.id }
    }
}

//region Exception

/**
 * Signifies that the call to check the approval status of a
 * [ProtonPaymentToken] has timed-out.
 *
 * @param message the upstream throwable error message.
 */
public class TokenPollingTimeoutException(message: String) : Exception(message)

/**
 * Signifies that a [ProtonPaymentToken] has not been approved, but conversely
 * disapproved.
 *
 * @param message the upstream throwable error message.
 */
public class TokenDisapprovedException(message: String) : Exception(message)

//endregion
