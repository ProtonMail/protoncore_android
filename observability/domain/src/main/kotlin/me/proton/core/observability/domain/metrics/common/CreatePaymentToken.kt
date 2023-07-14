/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.observability.domain.metrics.common

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import me.proton.core.network.domain.HttpResponseCodes.HTTP_BAD_REQUEST
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.BODY_PARSE_FAILURE
import me.proton.core.network.domain.ResponseCodes.CURRENCY_FORMAT
import me.proton.core.network.domain.ResponseCodes.HUMAN_VERIFICATION_REQUIRED
import me.proton.core.network.domain.ResponseCodes.INVALID_REQUIREMENTS
import me.proton.core.network.domain.ResponseCodes.INVALID_VALUE
import me.proton.core.network.domain.ResponseCodes.NOT_SAME_AS_FIELD
import me.proton.core.network.domain.ResponseCodes.VALUE_OUT_OF_BOUNDS
import me.proton.core.network.domain.hasProtonErrorCode

@Serializable
public data class CreatePaymentTokenLabels constructor(
    @get:Schema(required = true)
    val status: CreatePaymentTokenStatus
)

@Suppress("EnumNaming", "EnumEntryName")
public enum class CreatePaymentTokenStatus {
    http2xx,
    http400InvalidValue,
    http400ValueOutOfBounds,
    http400NotSameAsField,
    http400CurrencyFormat,
    http400BodyParseFailure,
    http422InvalidRequirements,
    http422HvRequired,
    http400,
    http422,
    http4xx,
    http5xx,
    connectionError,
    notConnected,
    parseError,
    sslError,
    unknown
}

internal fun Result<*>.toCreatePaymentTokenStatus(): CreatePaymentTokenStatus = when {
    isHttpError(HTTP_BAD_REQUEST) -> when {
        isProtonError(INVALID_VALUE) -> CreatePaymentTokenStatus.http400InvalidValue
        isProtonError(VALUE_OUT_OF_BOUNDS) -> CreatePaymentTokenStatus.http400ValueOutOfBounds
        isProtonError(NOT_SAME_AS_FIELD) -> CreatePaymentTokenStatus.http400NotSameAsField
        isProtonError(CURRENCY_FORMAT) -> CreatePaymentTokenStatus.http400CurrencyFormat
        isProtonError(BODY_PARSE_FAILURE) -> CreatePaymentTokenStatus.http400BodyParseFailure
        else -> CreatePaymentTokenStatus.http400
    }

    isHttpError(HTTP_UNPROCESSABLE) -> when {
        isProtonError(INVALID_REQUIREMENTS) -> CreatePaymentTokenStatus.http422InvalidRequirements
        isProtonError(HUMAN_VERIFICATION_REQUIRED) -> CreatePaymentTokenStatus.http422HvRequired
        else -> CreatePaymentTokenStatus.http422
    }

    else -> toHttpApiStatus().toUsernameAvailabilityStatus()
}

private fun Result<*>.isProtonError(protonCode: Int): Boolean =
    exceptionOrNull()?.hasProtonErrorCode(protonCode) == true

private fun HttpApiStatus.toUsernameAvailabilityStatus(): CreatePaymentTokenStatus =
    when (this) {
        HttpApiStatus.http2xx -> CreatePaymentTokenStatus.http2xx
        HttpApiStatus.http4xx -> CreatePaymentTokenStatus.http4xx
        HttpApiStatus.http5xx -> CreatePaymentTokenStatus.http5xx
        HttpApiStatus.connectionError -> CreatePaymentTokenStatus.connectionError
        HttpApiStatus.notConnected -> CreatePaymentTokenStatus.notConnected
        HttpApiStatus.parseError -> CreatePaymentTokenStatus.parseError
        HttpApiStatus.sslError -> CreatePaymentTokenStatus.sslError
        HttpApiStatus.unknown -> CreatePaymentTokenStatus.unknown
    }