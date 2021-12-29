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
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_FORBIDDEN
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.MissingScopes
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Test
import kotlin.test.BeforeTest

class MissingScopeHandlerTest {

    private val sessionId = mockk<SessionId>(relaxed = true)
    private val userId = UserId("test-user-id")

    private val sessionProvider = mockk<SessionProvider>()
    private val apiBackend = mockk<ApiBackend<Any>>()
    private val missingScopeListener = mockk<MissingScopeListener>(relaxed = true)

    @BeforeTest
    fun beforeTest() {
        coEvery { sessionProvider.getUserId(any()) } returns userId
    }

    @Test
    fun `test missing scope LOCKED called`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            HTTP_FORBIDDEN,
            "Missing Scope",
            ApiResult.Error.ProtonData(
                HTTP_FORBIDDEN,
                "Missing Scope",
                missingScopes = MissingScopes(listOf(Scope.LOCKED))
            )
        )

        coEvery { missingScopeListener.onMissingScope(userId, any()) } returns MissingScopeResult.Success
        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val missingScopeHandler =
            MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) { missingScopeListener.onMissingScope(userId, listOf(Scope.LOCKED)) }
    }

    @Test
    fun `test missing scope PASSWORD called`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            HTTP_FORBIDDEN,
            "Missing Scope",
            ApiResult.Error.ProtonData(
                HTTP_FORBIDDEN,
                "Missing Scope",
                missingScopes = MissingScopes(listOf(Scope.PASSWORD))
            )
        )

        coEvery { missingScopeListener.onMissingScope(userId, any()) } returns MissingScopeResult.Success
        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val missingScopeHandler = MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) { missingScopeListener.onMissingScope(userId, listOf(Scope.PASSWORD)) }
    }

    @Test
    fun `test missing scope LOCKED and PASSWORD called handled properly`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            HTTP_FORBIDDEN,
            "Missing Scope",
            ApiResult.Error.ProtonData(
                HTTP_FORBIDDEN,
                "Missing Scope",
                missingScopes = MissingScopes(listOf(Scope.LOCKED, Scope.PASSWORD))
            )
        )

        coEvery { missingScopeListener.onMissingScope(userId, any()) } returns MissingScopeResult.Success
        coEvery { apiBackend.invoke<Any>(any()) } returns ApiResult.Success("test")

        val missingScopeHandler =
            MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) { missingScopeListener.onMissingScope(userId, listOf(Scope.LOCKED, Scope.PASSWORD)) }
    }

    @Test
    fun `test missing scope called but retry api failed`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            HTTP_FORBIDDEN,
            "Missing Scope",
            ApiResult.Error.ProtonData(
                HTTP_FORBIDDEN,
                "Missing Scope",
                missingScopes = MissingScopes(listOf(Scope.PASSWORD))
            )
        )

        coEvery { missingScopeListener.onMissingScope(userId, any()) } returns MissingScopeResult.Success
        coEvery { apiBackend.invoke<Any>(any()) } returns apiResult

        val missingScopeHandler = MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = apiBackend,
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 1) { missingScopeListener.onMissingScope(userId, listOf(Scope.PASSWORD)) }
    }

    @Test
    fun `test missing scope not called on other errors`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            ApiResult.Error.ProtonData(
                2000,
                "Some error"
            )
        )

        val missingScopeHandler = MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) { missingScopeListener.onMissingScope(userId, any()) }
    }

    @Test
    fun `test no proton data does not crash the handler`() = runBlockingTest {
        val apiResult = ApiResult.Error.Http(
            422,
            "Some error",
            null
        )

        val missingScopeHandler = MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) { missingScopeListener.onMissingScope(userId, any()) }
    }

    @Test
    fun `test connectivity error does not invoke missing scope handler`() = runBlockingTest {
        val apiResult = ApiResult.Error.Connection(false)

        val missingScopeHandler = MissingScopeHandler<Any>(sessionId, sessionProvider, missingScopeListener)

        val result = missingScopeHandler.invoke(
            backend = mockk(),
            error = apiResult,
            call = mockk<ApiManager.Call<Any, Any>>()
        )

        assertNotNull(result)
        coVerify(exactly = 0) { missingScopeListener.onMissingScope(userId, any()) }
    }
}
