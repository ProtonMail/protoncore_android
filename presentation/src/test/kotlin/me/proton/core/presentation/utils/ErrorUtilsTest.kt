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

package me.proton.core.presentation.utils

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.exception.ApiConnectionException
import me.proton.core.presentation.R
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import kotlin.test.assertEquals

class ErrorUtilsTest {

    private val resources = mockk<Resources>(relaxed = true)

    private val connectionError = "test-connection-error"

    @Before
    fun beforeEveryTest() {
        every { resources.getString(R.string.presentation_general_connection_error) } returns connectionError
    }

    @Test
    fun `socket timeout exception returns original exception message`() {
        val exception = ApiException(ApiResult.Error.Connection(cause = SocketTimeoutException("test-message")))
        val message = exception.getLocalizedMessage(resources)
        assertEquals("test-message", message)
    }

    @Test
    fun `api connection exception as a result of socket timeout exception should return user friendly error message`() {
        val exception = ApiException(
            ApiResult.Error.Connection(
                cause = ApiConnectionException(
                    path = "test-path",
                    query = null,
                    originalException = SocketTimeoutException("test-message")
                )
            )
        )
        val message = exception.getLocalizedMessage(resources)
        assertEquals(connectionError, message)
    }

    @Test
    fun `any other exception returns original exception message`() {
        val exception = ApiException(
            ApiResult.Error.Http(
                cause = RuntimeException("test-message"),
                httpCode = 403,
                message = "http message"
            )
        )
        val message = exception.getLocalizedMessage(resources)
        assertEquals("test-message", message)
    }
}
