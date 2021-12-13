/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.network.domain.handlers

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.exception.ApiConnectionException
import me.proton.core.network.domain.serverconnection.ApiConnectionListener
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.net.SocketTimeoutException

class ApiConnectionHandlerTest {
    private val apiConnectionListener = mockk<ApiConnectionListener>(relaxed = true)

    private val apiBackend = mockk<ApiBackend<Any>>()

    @Test
    fun `test api connection called on connection error`() = runBlockingTest {
        val apiResult = ApiResult.Error.Connection(
            potentialBlock = true,
            cause = ApiConnectionException(
                path = "test-path",
                query = "test-query",
                originalException = SocketTimeoutException("test")
            )
        )

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val apiConnectionHandler = ApiConnectionHandler<Any>(apiConnectionListener)

        val result = apiConnectionHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) {
            apiConnectionListener.onPotentiallyBlocked<Any>("test-path", "test-query", any())
        }
    }

    @Test
    fun `test api connection NOT called potential blocking false`() = runBlockingTest {
        val apiResult = ApiResult.Error.Connection(
            potentialBlock = false,
            cause = ApiConnectionException(
                path = "test-path",
                query = "test-query",
                originalException = SocketTimeoutException("test")
            )
        )

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val apiConnectionHandler = ApiConnectionHandler<Any>(apiConnectionListener)

        val result = apiConnectionHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            apiConnectionListener.onPotentiallyBlocked<Any>(any(), any(), any())
        }
    }

    @Test
    fun `test api connection NOT called on other errors`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            null
        )

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val apiConnectionHandler = ApiConnectionHandler<Any>(apiConnectionListener)

        val result = apiConnectionHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) {
            apiConnectionListener.onPotentiallyBlocked<Any>(any(), any(), any())
        }
    }
}
