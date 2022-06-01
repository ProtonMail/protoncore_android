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

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Test
import java.net.ConnectException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<AuthenticationApi>>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: AuthRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUsername = "test-username"
    private val testAccessToken = "test-access-token"
    private val testClientSecret = "test-client-secret"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )
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
    private val successScopeInfo = mockk<ScopeInfo>()

    private val product = Product.Mail

    // endregion
    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, TestDispatcherProvider)
        every {
            apiManagerFactory.create(
                interfaceClass = AuthenticationApi::class
            )
        } returns apiManager
        every {
            apiManagerFactory.create(
                SessionId(testSessionId),
                interfaceClass = AuthenticationApi::class
            )
        } returns apiManager
        repository = AuthRepositoryImpl(apiProvider, context, product)
    }

    @Test
    fun `login info success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<LoginInfo>(any(), any()) } returns ApiResult.Success(successLoginInfo)
        // WHEN
        val loginInfoResponse = repository.getLoginInfo(testUsername, testClientSecret)
        // THEN
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
        val throwable = assertFailsWith(ApiException::class) {
            repository.getLoginInfo(testUsername, testClientSecret)
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `login success result`() = runBlockingTest {
        // GIVEN
        every { successSessionInfo.username } returns testUsername
        every { successSessionInfo.accessToken } returns testAccessToken
        every { successSessionInfo.serverProof } returns testSrpProofs.expectedServerProof
        coEvery { apiManager.invoke<SessionInfo>(any(), any()) } returns ApiResult.Success(successSessionInfo)
        // WHEN
        val sessionInfoResponse = repository.performLogin(
            testUsername,
            testClientSecret,
            testSrpProofs,
            testSrpSession,
            emptyList()
        )
        // THEN
        assertNotNull(sessionInfoResponse)
        assertEquals(testUsername, sessionInfoResponse.username)
        assertEquals(testAccessToken, sessionInfoResponse.accessToken)
    }

    @Test
    fun `login success result with frames`() = runBlockingTest {
        // GIVEN
        every { successSessionInfo.username } returns testUsername
        every { successSessionInfo.accessToken } returns testAccessToken
        every { successSessionInfo.serverProof } returns testSrpProofs.expectedServerProof
        coEvery { apiManager.invoke<SessionInfo>(any(), any()) } returns ApiResult.Success(successSessionInfo)
        // WHEN
        val sessionInfoResponse = repository.performLogin(
            testUsername,
            testClientSecret,
            testSrpProofs,
            testSrpSession,
            listOf(
                ChallengeFrameDetails(
                    flow = "test-flow",
                    challengeFrame = "test-challenge-frame",
                    focusTime = listOf(0),
                    clicks = 1,
                    copy = emptyList(),
                    paste = emptyList(),
                    keys = emptyList()
                )
            )
        )
        // THEN
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
        val throwable = assertFailsWith(ApiException::class) {
            repository.performLogin(
                testUsername,
                testClientSecret,
                testSrpProofs,
                testSrpSession,
                emptyList()
            )
        }
        // THEN
        assertEquals("test login error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `login error result with frames`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.performLogin(
                testUsername,
                testClientSecret,
                testSrpProofs,
                testSrpSession,
                listOf(
                    ChallengeFrameDetails(
                        flow = "test-flow",
                        challengeFrame = "test-challenge-frame",
                        focusTime = listOf(0),
                        clicks = 1,
                        copy = emptyList(),
                        paste = emptyList(),
                        keys = emptyList()
                    )
                )
            )
        }
        // THEN
        assertEquals("test login error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `login fails because server returns wrong SRP proof`() = runBlockingTest {
        // GIVEN
        val block = slot<suspend AuthenticationApi.() -> SessionInfo>()
        coEvery { apiManager.invoke(any(), capture(block)) } coAnswers {
            val mockedApiCall = mockk<AuthenticationApi> {
                coEvery { performLogin(any()) } returns mockk {
                    every { serverProof } returns testSrpProofs.expectedServerProof + "corrupted"
                }
            }
            val sessionInfo = block.captured(mockedApiCall)
            ApiResult.Success(sessionInfo)
        }
        // WHEN & THEN
        assertFailsWith<InvalidServerAuthenticationException> {
            repository.performLogin(
                testUsername,
                testClientSecret,
                testSrpProofs,
                testSrpSession,
                emptyList()
            )
        }
    }

    @Test
    fun `logout success result`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Success(true)
        // WHEN
        val response = repository.revokeSession(SessionId(testSessionId))
        // THEN
        assertTrue(response)
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
        assertTrue(response)
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
        assertTrue(response)
    }

    @Test
    fun `performSecondFactor code success result`() = runBlockingTest {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any(), any()) } returns ApiResult.Success(successScopeInfo)
        // WHEN
        val responseScopeInfo =
            repository.performSecondFactor(SessionId(testSessionId), SecondFactorProof.SecondFactorCode("123456"))
        // THEN
        assertEquals(successScopeInfo, responseScopeInfo)
        assertEquals("test-scope", responseScopeInfo.scope)
        assertEquals(2, responseScopeInfo.scopes.size)
    }

    @Test
    fun `performSecondFactor u2f success result`() = runBlockingTest {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any(), any()) } returns ApiResult.Success(successScopeInfo)
        // WHEN
        val responseScopeInfo = repository.performSecondFactor(
            SessionId(testSessionId),
            SecondFactorProof.SecondFactorSignature(
                "test-key-handle", "client-data", "test-signature-data"
            )
        )
        // THEN
        assertEquals(successScopeInfo, responseScopeInfo)
        assertEquals("test-scope", responseScopeInfo.scope)
        assertEquals(2, responseScopeInfo.scopes.size)
    }
}
