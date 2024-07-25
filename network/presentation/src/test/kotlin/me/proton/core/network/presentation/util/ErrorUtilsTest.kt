/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.network.presentation.util

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
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
    fun `apiResult Error Certificate return user friendly error message`() {
        val exception = ApiException(
            ApiResult.Error.Connection(cause = SocketTimeoutException("test-message"))
        )
        val message = exception.getUserMessage(resources)
        assertEquals(connectionError, message)
    }

    @Test
    fun `apiResult Error Connection return user friendly error message`() {
        val exception = ApiException(
            ApiResult.Error.Connection(cause = SocketTimeoutException("test-message"))
        )
        val message = exception.getUserMessage(resources)
        assertEquals(connectionError, message)
    }

    @Test
    fun `apiResult Error Timeout return user friendly error message`() {
        val exception = ApiException(
            ApiResult.Error.Connection(cause = SocketTimeoutException("test-message"))
        )
        val message = exception.getUserMessage(resources)
        assertEquals(connectionError, message)
    }

    @Test
    fun `apiResult Error Http returns original error cause message`() {
        val exception = ApiException(
            ApiResult.Error.Http(
                httpCode = 404,
                message = "test-message-not-found",
                cause = RuntimeException("test-message-runtime-exception")
            )
        )
        val message = exception.getUserMessage(resources)
        assertEquals("test-message-runtime-exception", message)
    }

    @Test
    fun `apiResult Error Parse returns original error cause message`() {
        val exception = ApiException(
            ApiResult.Error.Parse(
                cause = RuntimeException("test-message-parse-exception")
            )
        )
        val message = exception.getUserMessage(resources)
        assertEquals("test-message-parse-exception", message)
    }

    @Test
    fun `any other exception returns original exception message`() {
        val exception = IllegalStateException("test-message-illegal-exception")
        val message = exception.getUserMessage(resources)
        assertEquals("test-message-illegal-exception", message)
    }
}
