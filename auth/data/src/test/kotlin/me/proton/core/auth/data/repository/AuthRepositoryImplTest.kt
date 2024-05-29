/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.data.repository

import android.content.Context
import android.util.Base64
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.api.response.SecondFactorResponse
import me.proton.core.auth.data.api.response.SessionResponse
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.challenge.data.frame.ChallengeFrame
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.runTestWithResultContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.ConnectException
import kotlin.test.assertContentEquals
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
    private val testSessionId = SessionId("test-session-id")
    private val testUsername = "test-username"
    private val testAccessToken = "test-access-token"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )
    private val testSrpSession = "test-srp-session"

    private val successLoginInfo = AuthInfo.Srp(
        username = testUsername,
        modulus = "test-modulus",
        serverEphemeral = "test-serverephemeral",
        version = 1,
        salt = "test-salt",
        srpSession = "test-srpSession",
        secondFactor = null
    )

    private val successSessionInfo = mockk<SessionInfo>()
    private val successScopeInfo = mockk<ScopeInfo>()

    private val product = Product.Mail
    private val validateServerProof = ValidateServerProof()

    private val testDispatcherProvider = TestDispatcherProvider()

    // endregion
    @Before
    fun beforeEveryTest() {
        mockkObject(ChallengeFrame.Device.Companion)
        mockkStatic(Base64::class)
        coEvery { ChallengeFrame.Device.build(any()) } returns mockk()

        // GIVEN
        coEvery { sessionProvider.getSessionId(any()) } returns testSessionId
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, testDispatcherProvider)
        every {
            apiManagerFactory.create(
                interfaceClass = AuthenticationApi::class
            )
        } returns apiManager
        every {
            apiManagerFactory.create(
                testSessionId,
                interfaceClass = AuthenticationApi::class
            )
        } returns apiManager
        repository = AuthRepositoryImpl(apiProvider, context, product, validateServerProof)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `login info success result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<AuthInfo>(any()) } returns ApiResult.Success(
            successLoginInfo
        )
        // WHEN
        val loginInfoResponse = repository.getAuthInfoSrp(testSessionId, testUsername)
        // THEN
        assertNotNull(loginInfoResponse)
        assertEquals(testUsername, loginInfoResponse.username)
    }

    @Test
    fun `login info error result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<AuthInfo>(any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.getAuthInfoSrp(testSessionId, testUsername)
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `login success result`() = runTestWithResultContext(testDispatcherProvider.Main) {
        // GIVEN
        every { successSessionInfo.username } returns testUsername
        every { successSessionInfo.accessToken } returns testAccessToken
        every { successSessionInfo.serverProof } returns testSrpProofs.expectedServerProof
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Success(
            successSessionInfo
        )
        // WHEN
        val sessionInfoResponse = repository.performLogin(
            testUsername,
            testSrpProofs,
            testSrpSession,
            emptyList()
        )
        // THEN
        assertNotNull(sessionInfoResponse)
        assertEquals(testUsername, sessionInfoResponse.username)
        assertEquals(testAccessToken, sessionInfoResponse.accessToken)

        val result = assertSingleResult("performLogin")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `login success result with frames`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        every { successSessionInfo.username } returns testUsername
        every { successSessionInfo.accessToken } returns testAccessToken
        every { successSessionInfo.serverProof } returns testSrpProofs.expectedServerProof
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Success(
            successSessionInfo
        )
        // WHEN
        val sessionInfoResponse = repository.performLogin(
            testUsername,
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
    fun `login error result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.performLogin(
                testUsername,
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
    fun `login error result with frames`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.performLogin(
                testUsername,
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
    fun `login fails because server returns wrong SRP proof`() =
        runTest(testDispatcherProvider.Main) {
            // GIVEN
            val block = slot<suspend AuthenticationApi.() -> SessionInfo>()
            coEvery { apiManager.invoke(capture(block)) } coAnswers {
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
                    testSrpProofs,
                    testSrpSession,
                    emptyList()
                )
            }
        }

    @Test
    fun `login fails because server returns null SRP proof`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        val block = slot<suspend AuthenticationApi.() -> SessionInfo>()
        coEvery { apiManager.invoke(capture(block)) } coAnswers {
            val mockedApiCall = mockk<AuthenticationApi> {
                coEvery { performLogin(any()) } returns mockk {
                    every { serverProof } returns null
                }
            }
            val sessionInfo = block.captured(mockedApiCall)
            ApiResult.Success(sessionInfo)
        }
        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            repository.performLogin(
                testUsername,
                testSrpProofs,
                testSrpSession,
                emptyList()
            )
        }
    }

    @Test
    fun `logout success result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Success(true)
        // WHEN
        val response = repository.revokeSession(testSessionId)
        // THEN
        assertTrue(response)
    }

    @Test
    fun `logout api error result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        val response = repository.revokeSession(testSessionId)
        // THEN
        assertTrue(response)
    }

    @Test
    fun `logout connectivity error result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<Boolean>(any(), any()) } returns ApiResult.Error.Connection(
            isConnectedToNetwork = false, cause = ConnectException("connection refused")
        )
        // WHEN
        val response = repository.revokeSession(testSessionId)
        // THEN
        assertTrue(response)
    }

    @Test
    fun `performSecondFactor code success result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any()) } returns ApiResult.Success(
            successScopeInfo
        )
        // WHEN
        val responseScopeInfo =
            repository.performSecondFactor(
                testSessionId,
                SecondFactorProof.SecondFactorCode("123456")
            )
        // THEN
        assertEquals(successScopeInfo, responseScopeInfo)
        assertEquals("test-scope", responseScopeInfo.scope)
        assertEquals(2, responseScopeInfo.scopes.size)
    }

    @Test
    fun `performSecondFactor u2f success result`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        every { successScopeInfo.scope } returns "test-scope"
        every { successScopeInfo.scopes } returns listOf("scope1", "scope2")
        coEvery { apiManager.invoke<ScopeInfo>(any()) } returns ApiResult.Success(
            successScopeInfo
        )
        // WHEN
        val responseScopeInfo = repository.performSecondFactor(
            testSessionId,
            SecondFactorProof.SecondFactorSignature(
                "test-key-handle", "client-data", "test-signature-data"
            )
        )
        // THEN
        assertEquals(successScopeInfo, responseScopeInfo)
        assertEquals("test-scope", responseScopeInfo.scope)
        assertEquals(2, responseScopeInfo.scopes.size)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `performSecondFactor fido2`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        val blockSlot = slot<suspend AuthenticationApi.() -> ScopeInfo>()
        val authenticationApi = mockk<AuthenticationApi> {
            coEvery { performSecondFactor(any()) } returns SecondFactorResponse(
                "test-scope", scopes = listOf("scope1", "scope2")
            )
        }
        coEvery { apiManager.invoke(capture(blockSlot)) } coAnswers {
            ApiResult.Success(blockSlot.captured.invoke(authenticationApi))
        }
        every { Base64.encodeToString(any(), any()) } returns "encoded"

        // WHEN
        val responseScopeInfo = repository.performSecondFactor(
            testSessionId,
            SecondFactorProof.Fido2(
                publicKeyOptions = Fido2PublicKeyCredentialRequestOptions(
                    challenge = ubyteArrayOf(1U, 2U, 3U),
                    timeout = 600_000U,
                    rpId = "example.test"
                ),
                clientData = byteArrayOf(4, 5, 6),
                authenticatorData = byteArrayOf(7, 8, 9),
                signature = byteArrayOf(10, 11, 12),
                credentialID = byteArrayOf(13, 14, 15)
            )
        )

        // THEN
        assertEquals("test-scope", responseScopeInfo.scope)
        assertContentEquals(listOf("scope1", "scope2"), responseScopeInfo.scopes)
    }

    @Test
    fun `performLoginSso return SessionInfo`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Success(mockk())
        // WHEN
        repository.performLoginSso("username@domain.com", "token")
    }

    @Test(expected = ApiException::class)
    fun `performLoginSso return error`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        repository.performLoginSso("username@domain.com", "token")
    }

    @Test
    fun `performLoginLess return SessionInfo`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Success(mockk())
        // WHEN
        repository.performLoginLess()
    }

    @Test(expected = ApiException::class)
    fun `performLoginLess return error`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionInfo>(any()) } returns ApiResult.Error.Http(
            httpCode = 401,
            message = "test http error",
            proton = ApiResult.Error.ProtonData(1, "test login error")
        )
        // WHEN
        repository.performLoginLess()
    }

    @Test
    fun `requestSession success`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SessionResponse>(any(), any()) } returns ApiResult.Success(
            SessionResponse(
                accessToken = "accessToken",
                refreshToken = "refreshToken",
                tokenType = "Bearer",
                scopes = emptyList(),
                sessionId = "sessionId"
            )
        )
        // WHEN
        val session = repository.requestSession()
        // THEN
        assertNotNull(session)
    }

    @Test
    fun `refreshSession  success`() = runTest(testDispatcherProvider.Main) {
        // GIVEN
        val session = Session.Unauthenticated(
            sessionId = SessionId("sessionId"),
            accessToken = "accessToken",
            refreshToken = "refreshToken",
            scopes = emptyList(),
        )
        coEvery { apiManager.invoke<SessionResponse>(any(), any()) } returns ApiResult.Success(
            SessionResponse(
                accessToken = "accessToken",
                refreshToken = "refreshToken",
                tokenType = "Bearer",
                scopes = emptyList(),
                sessionId = "sessionId"
            )
        )
        // WHEN
        val refreshed = repository.refreshSession(session)
        // THEN
        assertNotNull(refreshed)
    }
}
