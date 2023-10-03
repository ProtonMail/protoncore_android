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

package me.proton.core.data.arch

import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.ApiResult
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResultMapperKtTest {
    @Test
    fun `success result`() {
        assertEquals(
            DataResult.Success(ResponseSource.Remote, "ok"),
            ApiResult.Success("ok").toDataResult()
        )
    }

    @Test
    fun `http error result`() {
        val cause = Throwable("HTTP 400")
        val protonCode = 123

        assertEquals(
            expected = DataResult.Error.Remote(
                message = "Proton error",
                cause = cause,
                protonCode = protonCode,
                httpCode = 400
            ),
            actual = ApiResult.Error.Http(
                400,
                "Bad request",
                proton = ApiResult.Error.ProtonData(code = protonCode, error = "Proton error"),
                cause = cause
            ).toDataResult()
        )

        assertEquals(
            DataResult.Error.Remote(message = "Bad request", cause = null),
            ApiResult.Error.Http(httpCode = 0, message = "Bad request").toDataResult()
        )
    }

    @Test
    fun `parse error result`() {
        val cause = Throwable("Invalid JSON")
        assertEquals(
            DataResult.Error.Remote(message = "Invalid JSON", cause = cause),
            ApiResult.Error.Parse(cause).toDataResult()
        )

        assertEquals(
            DataResult.Error.Remote(message = null, cause = null),
            ApiResult.Error.Parse(cause = null).toDataResult()
        )
    }

    @Test
    fun `connection error result`() {
        val cause = Throwable("No Internet")
        assertEquals(
            DataResult.Error.Remote(message = "No Internet", cause = cause),
            ApiResult.Error.Connection(cause = cause).toDataResult()
        )

        assertEquals(
            DataResult.Error.Remote(message = null, cause = null),
            ApiResult.Error.Connection().toDataResult()
        )
    }
}
