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

internal class UsernameAvailabilityKtTest {
    @Test
    fun httpSuccess() {
        assertEquals(
            UsernameAvailabilityStatus.http2xx,
            Result.success(Unit).toUsernameAvailabilityStatus()
        )
    }

    @Test
    fun httpErrors() {
        assertEquals(
            UsernameAvailabilityStatus.http409UsernameConflict,
            makeHttpErrorResult(
                409,
                "conflict",
                ApiResult.Error.ProtonData(
                    ResponseCodes.USER_EXISTS_USERNAME_ALREADY_USED,
                    "Username used"
                )
            ).toUsernameAvailabilityStatus()
        )

        assertEquals(
            UsernameAvailabilityStatus.http409,
            makeHttpErrorResult(409, "conflict").toUsernameAvailabilityStatus()
        )

        assertEquals(
            UsernameAvailabilityStatus.http422,
            makeHttpErrorResult(422, "unprocessable").toUsernameAvailabilityStatus()
        )

        assertEquals(
            UsernameAvailabilityStatus.http4xx,
            makeHttpErrorResult(400, "invalid").toUsernameAvailabilityStatus()
        )

        assertEquals(
            UsernameAvailabilityStatus.http5xx,
            makeHttpErrorResult(500, "error").toUsernameAvailabilityStatus()
        )
    }

    @Test
    fun otherErrors() {
        assertEquals(
            UsernameAvailabilityStatus.connectionError,
            Result.failure<Unit>(ApiException(ApiResult.Error.Connection(isConnectedToNetwork = true)))
                .toUsernameAvailabilityStatus()
        )
        assertEquals(
            UsernameAvailabilityStatus.notConnected,
            Result.failure<Unit>(ApiException(ApiResult.Error.Connection(isConnectedToNetwork = false)))
                .toUsernameAvailabilityStatus()
        )
        assertEquals(
            UsernameAvailabilityStatus.parseError,
            Result.failure<Unit>(ApiException(ApiResult.Error.Parse(null)))
                .toUsernameAvailabilityStatus()
        )
        assertEquals(
            UsernameAvailabilityStatus.sslError,
            Result.failure<Unit>(SSLException("error")).toUsernameAvailabilityStatus()
        )
        assertEquals(
            UsernameAvailabilityStatus.unknown,
            Result.failure<Unit>(Throwable("Unknown error")).toUsernameAvailabilityStatus()
        )
    }

    private fun makeHttpErrorResult(
        httpCode: Int,
        httpMessage: String,
        protonData: ApiResult.Error.ProtonData? = null
    ): Result<*> =
        Result.failure<Unit>(ApiException(ApiResult.Error.Http(httpCode, httpMessage, protonData)))
}
