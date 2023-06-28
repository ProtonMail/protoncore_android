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

package me.proton.core.observability.domain.metrics

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.domain.metrics.common.AccountTypeLabels
import kotlin.test.Test
import kotlin.test.assertEquals

class SignupAccountCreationTotalKtTest {
    @Test
    fun `result to http422HvRequired`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNPROCESSABLE,
                "Unprocessable",
                ApiResult.Error.ProtonData(
                    code = ResponseCodes.HUMAN_VERIFICATION_REQUIRED,
                    error = "HV error"
                )
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http422HvRequired,
            data.Labels.status
        )
    }

    @Test
    fun `result to http409UsernameConflict`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_CONFLICT,
                "Conflict",
                ApiResult.Error.ProtonData(
                    code = ResponseCodes.NOT_ALLOWED,
                    error = "Not allowed"
                )
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http409UsernameConflict,
            data.Labels.status
        )
    }

    @Test
    fun `result to http400`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_BAD_REQUEST, "Bad request"),
            AccountTypeLabels.username
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http400,
            data.Labels.status
        )
    }

    @Test
    fun `result to http401`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_UNAUTHORIZED, "Unauthorized"),
            AccountTypeLabels.external
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http401,
            data.Labels.status
        )
    }

    @Test
    fun `result to http409`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_CONFLICT, "Conflict"),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http409,
            data.Labels.status
        )
    }

    @Test
    fun `result to http422`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_UNPROCESSABLE, "Unprocessable"),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http422,
            data.Labels.status
        )
    }

    @Test
    fun `result to http2xx`() {
        val data = SignupAccountCreationTotal(
            Result.success(Any()),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http2xx,
            data.Labels.status
        )
    }

    @Test
    fun `result to http4xx`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_TOO_MANY_REQUESTS, "Too Many"),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http4xx,
            data.Labels.status
        )
    }

    @Test
    fun `result to http5xx`() {
        val data = SignupAccountCreationTotal(
            makeHttpFailure(HttpResponseCodes.HTTP_SERVICE_UNAVAILABLE, "Unavailable"),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.http5xx,
            data.Labels.status
        )
    }

    @Test
    fun `result to connectionError`() {
        val data = SignupAccountCreationTotal(
            Result.failure<Any>(
                ApiException(ApiResult.Error.Connection(isConnectedToNetwork = true))
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.connectionError,
            data.Labels.status
        )
    }

    @Test
    fun `result to notConnected`() {
        val data = SignupAccountCreationTotal(
            Result.failure<Any>(
                ApiException(ApiResult.Error.Connection(isConnectedToNetwork = false))
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.notConnected,
            data.Labels.status
        )
    }

    @Test
    fun `result to parseError`() {
        val data = SignupAccountCreationTotal(
            Result.failure<Any>(
                ApiException(ApiResult.Error.Parse(null))
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.parseError,
            data.Labels.status
        )
    }

    @Test
    fun `result to sslError`() {
        val data = SignupAccountCreationTotal(
            Result.failure<Any>(
                ApiException(ApiResult.Error.Certificate(Throwable("Invalid certificate")))
            ),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.sslError,
            data.Labels.status
        )
    }

    @Test
    fun `result to unknown`() {
        val data = SignupAccountCreationTotal(
            Result.failure<Any>(Throwable("Unknown error.")),
            AccountTypeLabels.internal
        )
        assertEquals(
            SignupAccountCreationTotal.ApiStatus.unknown,
            data.Labels.status
        )
    }

    private fun makeHttpFailure(
        httpCode: Int,
        httpMessage: String,
        protonData: ApiResult.Error.ProtonData? = null
    ): Result<Any> =
        Result.failure(
            ApiException(
                ApiResult.Error.Http(httpCode, httpMessage, protonData)
            )
        )
}
