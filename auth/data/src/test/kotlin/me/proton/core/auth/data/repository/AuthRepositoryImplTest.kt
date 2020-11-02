/*
 * Copyright (c) 2020 Proton Technologies AG
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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.domain.entity.KeySalt
import me.proton.core.auth.domain.entity.KeySalts
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.domain.arch.DataResult
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Before
import org.junit.Test
import java.net.ConnectException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class AuthRepositoryImplTest {

    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<AuthenticationApi>>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: AuthRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUsername = "test-username"
    private val testAccessToken = "test-access-token"
    private val testClientSecret = "test-client-secret"
    private val testClientEphemeral = "test-client-ephemeral"
    private val testClientProof = "test-client-proof"
    private val testSrpSession = "test-srp-session"

    private val successLoginInfo = LoginInfo(
        testUsername,
        "test-modulus",
        "test-serverephemeral",
        1,
        "test-salt",
        "test-srpSession"
    )

    private val successSessionInfo = mockk<SessionInfo>()
    private val successKeySalts = mockk<KeySalts>()
    private val successUser = mockk<User>()
    private val successScopeInfo = mockk<ScopeInfo>()

    // endregion
    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider)
        every { apiFactory.create(interfaceClass = AuthenticationApi::class) } returns apiManager
        every { apiFactory.create(SessionId(testSessionId), interfaceClass = AuthenticationApi::class) } returns apiManager
        repository = AuthRepositoryImpl(apiProvider)
    }

    @Test
    fun `login info success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<LoginInfo>(any(), any()) } returns ApiResult.Success(successLoginInfo)
        // WHEN
        val response = repository.getLoginInfo(testUsername, testClientSecret)
        // THEN
        assertTrue(response is DataResult.Success)
        val loginInfoResponse = response.value
        assertNotNull(loginInfoResponse)
        assertEquals(testUsername, loginInfoResponse.username)
    }

    @Test
    fun `login info error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<LoginInfo>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val response = repository.getLoginInfo(testUsername, testClientSecret)
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals(1, response.protonCode)
        assertEquals("test error", response.message)
    }

    @Test
    fun `login success result`() = runBlockingTest {
        // GIVEN
        every { successSessionInfo.username } returns testUsername
        every { successSessionInfo.accessToken } returns testAccessToken
        coEvery { apiManager.invoke<SessionInfo>(any(), any()) } returns ApiResult.Success(successSessionInfo)
        // WHEN
        val response = repository.performLogin(
            testUsername,
            testClientSecret,
            testClientEphemeral,
            testClientProof,
            testSrpSession
        )
        // THEN
        assertTrue(response is DataResult.Success)
        val sessionInfoResponse = response.value
        assertNotNull(sessionInfoResponse)
        assertEquals(testUsername, sessionInfoResponse.username)
        assertEquals(testAccessToken, sessionInfoResponse.accessToken)
    }

    @Test
    fun `login error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val response = repository.performLogin(
            testUsername,
            testClientSecret,
            testClientEphemeral,
            testClientProof,
            testSrpSession
        )
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals(1, response.protonCode)
        assertEquals("test login error", response.message)
    }

    @Test
    fun `logout success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Success(true)
        // WHEN
        val response = repository.revokeSession(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Success)
        assertNotNull(response.value)
        assertTrue(response.value)
    }

    @Test
    fun `logout api error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val response = repository.revokeSession(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals("test login error", response.message)
        assertEquals(1, response.protonCode)
    }

    @Test
    fun `logout connectivity error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Connection(
            potentialBlock = false, cause = ConnectException("connection refused")
        )
        // WHEN
        val response = repository.revokeSession(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals("connection refused", response.message)
        assertEquals(0, response.protonCode)
    }

    @Test
    fun `getSalts success result`() = runBlockingTest {
        // GIVEN
        every { successKeySalts.salts } returns listOf(KeySalt("test-salt-id", "test-salt"))
        coEvery { apiManager.invoke<KeySalts>(any(), any()) } returns ApiResult.Success(successKeySalts)
        // WHEN
        val response = repository.getSalts(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Success)
        assertNotNull(response.value)
        assertEquals(successKeySalts, response.value)
    }

    @Test
    fun `getSalts api error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<KeySalts>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test key salts error")
        )
        // WHEN
        val response = repository.getSalts(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals(1, response.protonCode)
        assertEquals("test key salts error", response.message)
    }

    @Test
    fun `getSalts connectivity error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<KeySalts>(any(), any()) } returns ApiResult.Error.Connection(
            potentialBlock = false, cause = ConnectException("connection refused")
        )
        // WHEN
        val response = repository.getSalts(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals("connection refused", response.message)
        assertEquals(0, response.protonCode)
    }

    @Test
    fun `getUser success result`() = runBlockingTest {
        // GIVEN
        every { successUser.name } returns "test-user-name"
        every { successUser.email } returns "test-email@test.com"
        coEvery { apiManager.invoke<User>(any(), any()) } returns ApiResult.Success(successUser)
        // WHEN
        val response = repository.getUser(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Success)
        assertNotNull(response.value)
        assertEquals(successUser, response.value)
    }

    @Test
    fun `getUser api error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<User>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test user error")
        )
        // WHEN
        val response = repository.getUser(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals(1, response.protonCode)
        assertEquals("test user error", response.message)
    }

    @Test
    fun `getUser connectivity error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<User>(any(), any()) } returns ApiResult.Error.Connection(
            potentialBlock = false, cause = ConnectException("connection refused")
        )
        // WHEN
        val response = repository.getUser(SessionId(testSessionId))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals("connection refused", response.message)
        assertEquals(0, response.protonCode)
    }

    @Test
    fun `performSecondFactor code success result`() = runBlockingTest {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any(), any()) } returns ApiResult.Success(successScopeInfo)
        // WHEN
        val response = repository.performSecondFactor(SessionId(testSessionId), SecondFactorProof.SecondFactorCode("123456"))
        // THEN
        assertTrue(response is DataResult.Success)
        assertNotNull(response.value)
        assertEquals(successScopeInfo, response.value)
        assertEquals("test-scope", response.value.scope)
        assertEquals(2, response.value.scopes.size)
    }

    @Test
    fun `performSecondFactor u2f success result`() = runBlockingTest {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any(), any()) } returns ApiResult.Success(successScopeInfo)
        // WHEN
        val response = repository.performSecondFactor(
            SessionId(testSessionId),
            SecondFactorProof.SecondFactorSignature(
                "test-key-handle", "client-data", "test-signature-data"
            )
        )
        // THEN
        assertTrue(response is DataResult.Success)
        assertNotNull(response.value)
        assertEquals(successScopeInfo, response.value)
        assertEquals("test-scope", response.value.scope)
        assertEquals(2, response.value.scopes.size)
    }

    @Test
    fun `performSecondFactor api error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<User>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test 2fa error")
        )
        // WHEN
        val response = repository.performSecondFactor(SessionId(testSessionId), SecondFactorProof.SecondFactorCode("123456"))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals(1, response.protonCode)
        assertEquals("test 2fa error", response.message)
    }

    @Test
    fun `performSecondFactor connectivity error result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<User>(any(), any()) } returns ApiResult.Error.Connection(
            potentialBlock = false, cause = ConnectException("connection refused")
        )
        // WHEN
        val response = repository.performSecondFactor(SessionId(testSessionId), SecondFactorProof.SecondFactorCode("123456"))
        // THEN
        assertTrue(response is DataResult.Error.Remote)
        assertEquals("connection refused", response.message)
        assertEquals(0, response.protonCode)
    }
}
