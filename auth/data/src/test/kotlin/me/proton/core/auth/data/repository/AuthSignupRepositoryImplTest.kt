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

package me.proton.core.auth.data.repository

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthSignupRepositoryImplTest {

    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<AuthenticationApi>>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: AuthRepositoryImpl
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider)
        every {
            apiManagerFactory.create(
                interfaceClass = AuthenticationApi::class
            )
        } returns apiManager
        repository = AuthRepositoryImpl(apiProvider, context)
    }

    @Test
    fun `validate email returns success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Success(true)
        // WHEN
        val response = repository.validateEmail("test-email")
        // THEN
        assertTrue(response)
    }

    @Test
    fun `validate email returns error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test email validation error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.validateEmail("test-email")
        }
        // THEN
        assertEquals("test email validation error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `validate phone returns success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Success(true)
        // WHEN
        val response = repository.validatePhone("test-phone")
        // THEN
        assertTrue(response)
    }

    @Test
    fun `validate phone returns error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test phone validation error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.validateEmail("test-phone")
        }
        // THEN
        assertEquals("test phone validation error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }
}
