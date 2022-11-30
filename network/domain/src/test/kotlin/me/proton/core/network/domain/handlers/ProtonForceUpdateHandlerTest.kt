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
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProtonForceUpdateHandlerTest {

    private val apiBackend = mockk<ApiBackend<Any>>()
    private val apiClient = mockk<ApiClient>(relaxed = true)

    @Test
    fun `test force update called on bad version error`() = runTest {
        val apiResult = ApiResult.Error.Http(
            403,
            "Force update",
            ApiResult.Error.ProtonData(
                ResponseCodes.APP_VERSION_BAD,
                "test-error"
            )
        )

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val forceUpdateHandler = ProtonForceUpdateHandler<Any>(apiClient)

        val result = forceUpdateHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )
        assertNotNull(result)
        coVerify(exactly = 1) {
            apiClient.forceUpdate("test-error")
        }
    }

    @Test
    fun `test force update called on invalid api version`() = runTest {
        val apiResult = ApiResult.Error.Http(
            403,
            "Force update",
            ApiResult.Error.ProtonData(
                ResponseCodes.API_VERSION_INVALID,
                "test-error"
            )
        )

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val forceUpdateHandler = ProtonForceUpdateHandler<Any>(apiClient)

        val result = forceUpdateHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )
        assertNotNull(result)
        coVerify(exactly = 1) {
            apiClient.forceUpdate("test-error")
        }
    }

    @Test
    fun `test force update not invoked on other errors`() = runTest {
        val apiResult = ApiResult.Error.Connection(false)

        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")
        val forceUpdateHandler = ProtonForceUpdateHandler<Any>(apiClient)

        val result = forceUpdateHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) { apiClient.forceUpdate(any()) }
    }
}
