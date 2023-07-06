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
import javax.net.ssl.SSLException
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpApiStatusKtTest {
    @Test
    fun convertResultToHttpApiStatus() {
        assertEquals(HttpApiStatus.http2xx, Result.success("OK").toHttpApiStatus())

        assertEquals(
            HttpApiStatus.sslError,
            errorResult(ApiResult.Error.Certificate(Throwable("Invalid certificate")))
                .toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.connectionError,
            errorResult(ApiResult.Error.Connection(isConnectedToNetwork = true))
                .toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.notConnected,
            errorResult(
                ApiResult.Error.Connection(isConnectedToNetwork = false)
            ).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.http4xx,
            errorResult(
                ApiResult.Error.Http(
                    httpCode = 400,
                    message = "Bad request"
                )
            ).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.http5xx,
            errorResult(
                ApiResult.Error.Http(
                    httpCode = 500,
                    message = "Error"
                )
            ).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.unknown,
            errorResult(
                ApiResult.Error.Http(
                    httpCode = 0,
                    message = "Error"
                )
            ).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.parseError,
            errorResult(
                ApiResult.Error.Parse(null)
            ).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.sslError,
            Result.failure<Any>(SSLException("Error")).toHttpApiStatus()
        )

        assertEquals(
            HttpApiStatus.unknown,
            Result.failure<Any>(Throwable("Error")).toHttpApiStatus()
        )
    }

    private fun errorResult(error: ApiResult.Error): Result<ApiResult<*>> =
        Result.failure(ApiException(error))
}
