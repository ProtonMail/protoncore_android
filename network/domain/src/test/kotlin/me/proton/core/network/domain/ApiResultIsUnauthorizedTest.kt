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

package me.proton.core.network.domain

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ApiResultIsUnauthorizedTest(
    private val expected: Boolean,
    private val actualResult: ApiResult<Any>,
) {

    @Test
    fun `expected result isUnauthorized`() {
        assertEquals(
            expected = expected,
            actual = actualResult.isUnauthorized(),
            message = "Assertion failed on $actualResult"
        )
    }

    companion object {
        @get:Parameterized.Parameters
        @get:JvmStatic
        @Suppress("unused")
        val data = listOf(
            arrayOf(false, ApiResult.Success("Any")),
            arrayOf(false, ApiResult.Error.Connection()),
            arrayOf(false, ApiResult.Error.NoInternet()),
            arrayOf(false, ApiResult.Error.Timeout(true)),
            arrayOf(false, ApiResult.Error.Certificate(Throwable())),
            arrayOf(false, ApiResult.Error.Parse(null)),
            arrayOf(false, ApiResult.Error.Http(400, "Bad Request")),
            arrayOf(true, ApiResult.Error.Http(401, "Unauthorized")), // <- Only one to be true.
            arrayOf(false, ApiResult.Error.Http(408, "Request Timeout")),
            arrayOf(false, ApiResult.Error.Http(429, "Too Many Requests")),
        )
            .asSequence()
            .plus((100..199).map { arrayOf(false, ApiResult.Error.Http(it, "Informational")) })
            .plus((200..299).map { arrayOf(false, ApiResult.Error.Http(it, "Success")) })
            .plus((300..399).map { arrayOf(false, ApiResult.Error.Http(it, "Redirection")) })
            .plus((402..499).map { arrayOf(false, ApiResult.Error.Http(it, "Client Error")) })
            .plus((500..599).map { arrayOf(false, ApiResult.Error.Http(it, "Server Error")) })
            .toList()

    }
}
