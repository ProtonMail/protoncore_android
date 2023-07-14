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

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import javax.net.ssl.SSLException
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CreatePaymentTokenKtTest {
    @Test
    fun httpSuccess() {
        assertEquals(
            CreatePaymentTokenStatus.http2xx,
            Result.success(Unit).toCreatePaymentTokenStatus()
        )
    }

    @Test
    fun httpErrors() {
        assertEquals(
            CreatePaymentTokenStatus.http400InvalidValue,
            makeHttpErrorResult(
                400, "bad request",
                ApiResult.Error.ProtonData(ResponseCodes.INVALID_VALUE, "Invalid value")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http400ValueOutOfBounds,
            makeHttpErrorResult(
                400, "bad request",
                ApiResult.Error.ProtonData(ResponseCodes.VALUE_OUT_OF_BOUNDS, "Out of bounds")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http400NotSameAsField,
            makeHttpErrorResult(
                400, "bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_SAME_AS_FIELD, "Not same as field")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http400CurrencyFormat,
            makeHttpErrorResult(
                400, "bad request",
                ApiResult.Error.ProtonData(ResponseCodes.CURRENCY_FORMAT, "Currency format")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http400BodyParseFailure,
            makeHttpErrorResult(
                400, "bad request",
                ApiResult.Error.ProtonData(ResponseCodes.BODY_PARSE_FAILURE, "Body parse")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http422InvalidRequirements,
            makeHttpErrorResult(
                422, "Unprocessable",
                ApiResult.Error.ProtonData(
                    ResponseCodes.INVALID_REQUIREMENTS,
                    "Invalid requirements"
                )
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http422HvRequired,
            makeHttpErrorResult(
                422, "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.HUMAN_VERIFICATION_REQUIRED, "HV required")
            ).toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http400,
            makeHttpErrorResult(400, "bad request").toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http422,
            makeHttpErrorResult(422, "unprocessable").toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http4xx,
            makeHttpErrorResult(401, "unauthorized").toCreatePaymentTokenStatus()
        )

        assertEquals(
            CreatePaymentTokenStatus.http5xx,
            makeHttpErrorResult(500, "error").toCreatePaymentTokenStatus()
        )
    }

    @Test
    fun otherErrors() {
        assertEquals(
            CreatePaymentTokenStatus.connectionError,
            Result.failure<Unit>(ApiException(ApiResult.Error.Connection(isConnectedToNetwork = true)))
                .toCreatePaymentTokenStatus()
        )
        assertEquals(
            CreatePaymentTokenStatus.notConnected,
            Result.failure<Unit>(ApiException(ApiResult.Error.Connection(isConnectedToNetwork = false)))
                .toCreatePaymentTokenStatus()
        )
        assertEquals(
            CreatePaymentTokenStatus.parseError,
            Result.failure<Unit>(ApiException(ApiResult.Error.Parse(null)))
                .toCreatePaymentTokenStatus()
        )
        assertEquals(
            CreatePaymentTokenStatus.sslError,
            Result.failure<Unit>(SSLException("error")).toCreatePaymentTokenStatus()
        )
        assertEquals(
            CreatePaymentTokenStatus.unknown,
            Result.failure<Unit>(Throwable("Unknown error")).toCreatePaymentTokenStatus()
        )
    }

    private fun makeHttpErrorResult(
        httpCode: Int,
        httpMessage: String,
        protonData: ApiResult.Error.ProtonData? = null
    ): Result<*> =
        Result.failure<Unit>(ApiException(ApiResult.Error.Http(httpCode, httpMessage, protonData)))
}