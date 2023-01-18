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

package me.proton.core.network.data

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.challenge.data.deviceFontSize
import me.proton.core.challenge.data.deviceInputMethods
import me.proton.core.challenge.data.deviceRegion
import me.proton.core.challenge.data.deviceStorage
import me.proton.core.challenge.data.nightMode
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.TokenResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.UnAuthSessionsRepository
import me.proton.core.network.domain.session.Session
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UnAuthSessionsRepositoryImplTest {

    private val apiManager = mockk<ApiManager<BaseRetrofitApi>>(relaxed = true)
    private val provider = mockk<ApiProvider>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private val product = Product.Mail

    private lateinit var repository: UnAuthSessionsRepository

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.challenge.data.DeviceUtilsKt")
        coEvery { provider.get<BaseRetrofitApi>() } returns apiManager

        every { context.deviceRegion() } returns "test-region"
        every { context.deviceFontSize() } returns 1.0f
        coEvery { context.deviceStorage() } returns 1.0
        every { context.nightMode() } returns false
        every { context.deviceInputMethods() } returns listOf("test-im")

        repository = UnAuthSessionsRepositoryImpl(provider, context, product)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.challenge.data.DeviceUtilsKt")
    }

    @Test
    fun `successful response handled properly`() = runTest {
        val block = slot<suspend BaseRetrofitApi.() -> Session>()
        coEvery { apiManager.invoke(any(), capture(block)) } coAnswers {
            val mockedApiCall = mockk<BaseRetrofitApi> {
                coEvery { requestToken(any()) } returns TokenResponse(
                    "test-at",
                    "test-rt",
                    "test-tt",
                    emptyList(),
                    "test-sessionId"
                )
            }
            val sessionInfo = block.captured(mockedApiCall)
            ApiResult.Success(sessionInfo)
        }
        val result = repository.requestToken()

        assertEquals("test-at", result.accessToken)
        assertEquals("test-rt", result.refreshToken)
    }

    @Test
    fun `error response handled properly`() = runTest {
        // GIVEN
        coEvery { apiManager.invoke<TokenResponse>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.requestToken()
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }
}