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

package me.proton.core.network.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ApiResultTest {
    @Test
    fun protonErrorCodes() {
        assertFalse(Throwable("Custom error").hasProtonErrorCode(1000))

        assertFalse(
            makeThrowableApiException(
                ApiResult.Error.Http(
                    200,
                    "Error",
                    ApiResult.Error.ProtonData(1000, "error")
                )
            ).hasProtonErrorCode(1234)
        )

        assertFalse(
            makeThrowableApiException(ApiResult.Error.Http(500, "Error")).hasProtonErrorCode(1000)
        )
        assertFalse(
            makeThrowableApiException(ApiResult.Error.Connection()).hasProtonErrorCode(1000)
        )

        assertTrue(
            makeThrowableApiException(
                ApiResult.Error.Http(
                    400,
                    "Error",
                    ApiResult.Error.ProtonData(8000, "error")
                )
            ).hasProtonErrorCode(8000)
        )
    }

    private fun makeThrowableApiException(err: ApiResult.Error): Throwable = ApiException(err)
}
