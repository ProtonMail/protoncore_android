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

package me.proton.core.network.data

import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.proton.core.network.data.util.MockApiClient
import me.proton.core.network.data.util.MockClientId
import me.proton.core.network.data.util.MockLogger
import me.proton.core.network.data.util.MockNetworkPrefs
import me.proton.core.network.data.util.MockSession
import me.proton.core.network.data.util.MockSessionListener
import me.proton.core.network.data.util.TestRetrofitApi
import me.proton.core.network.data.util.TestTLSHelper
import me.proton.core.network.data.util.prepareResponse
import me.proton.core.network.data.util.takeRequestWithDefaultTimeout
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.ClientVersionValidator
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.equalsNoCase
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Human verification related tests.
 */
@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
internal class HumanVerificationTests {

    private val humanVerificationResponse =
        """
            {
                "Code": 9001,
                "Error": "Human verification required",
                "ErrorDescription": "",
                "Details": {
                    "HumanVerificationMethods" : [
                        "captcha",
                        "email",
                        "sms"
                    ],
                    "HumanVerificationToken": "test"
                }
            }
        """.trimIndent()

    private val otherDetailsResponse =
        """
            {
                "Code": 9003,
                "Error": "Something other happened",
                "ErrorDescription": "",
                "Details": {
                    "SomethingOther" : "something other"
                }
            }
        """.trimIndent()

    private val scope = TestScope()

    private val testTlsHelper = TestTLSHelper()
    private lateinit var apiManagerFactory: ApiManagerFactory
    private lateinit var webServer: MockWebServer

    private lateinit var backend: ProtonApiBackend<TestRetrofitApi>
    private lateinit var logger: MockLogger
    private lateinit var client: MockApiClient

    private lateinit var session: Session
    private lateinit var clientId: ClientId

    private var clientIdProvider = mockk<ClientIdProvider>()
    private var serverTimeListener = mockk<ServerTimeListener>()
    private var sessionProvider = mockk<SessionProvider>()
    private val humanVerificationProvider = mockk<HumanVerificationProvider>()
    private val humanVerificationListener = mockk<HumanVerificationListener>()
    private val deviceVerificationProvider = mockk<DeviceVerificationProvider>()
    private val deviceVerificationListener = mockk<DeviceVerificationListener>()
    private val missingScopeListener = mockk<MissingScopeListener>(relaxed = true)
    private val clientVersionValidator = mockk<ClientVersionValidator> {
        every { validate(any()) } returns true
    }

    private var sessionListener: SessionListener = MockSessionListener(
        onTokenRefreshed = { session -> this.session = session },
        onTokenCreated = { session -> this.session = session }
    )
    private val cookieJar = mockk<ProtonCookieStore>()

    private var isNetworkAvailable = true
    private val networkManager = mockk<NetworkManager>()

    private lateinit var prefs: NetworkPrefs

    @BeforeTest
    fun before() {
        MockKAnnotations.init(this)

        logger = MockLogger()
        client = MockApiClient()
        prefs = MockNetworkPrefs()

        session = MockSession.getDefault()
        clientId = MockClientId.getForSession(session.sessionId)
        coEvery { clientIdProvider.getClientId(any()) } returns clientId

        coEvery { sessionProvider.getSessionId(any()) } returns session.sessionId
        coEvery { sessionProvider.getSession(any()) } returns session
        every { cookieJar.loadForRequest(any()) } returns emptyList()

        apiManagerFactory =
            ApiManagerFactory(
                "https://example.com/".toHttpUrl(),
                client,
                clientIdProvider,
                serverTimeListener,
                networkManager,
                prefs,
                sessionProvider,
                sessionListener,
                humanVerificationProvider,
                humanVerificationListener,
                deviceVerificationProvider,
                deviceVerificationListener,
                missingScopeListener,
                cookieJar,
                scope,
                cache = { null },
                clientVersionValidator = clientVersionValidator,
                dohAlternativesListener = null,
                okHttpClient = OkHttpClient()
            )
        every { networkManager.isConnectedToNetwork() } returns isNetworkAvailable

        isNetworkAvailable = true
        webServer = testTlsHelper.createMockServer()

        backend = createBackend(session.sessionId) {
            testTlsHelper.initPinning(it, TestTLSHelper.TEST_PINS)
        }
    }

    private fun createBackend(sessionId: SessionId?, pinningInit: (OkHttpClient.Builder) -> Unit) =
        ProtonApiBackend(
            webServer.url("/").toString(),
            client,
            clientIdProvider,
            serverTimeListener,
            sessionId,
            sessionProvider,
            humanVerificationProvider,
            deviceVerificationProvider,
            apiManagerFactory.baseOkHttpClient,
            listOf(
                ScalarsConverterFactory.create(),
                apiManagerFactory.jsonConverter
            ),
            TestRetrofitApi::class,
            networkManager,
            pinningInit,
            prefs,
            cookieJar,
        )

    @After
    fun after() {
        webServer.shutdown()
    }

    @Test
    fun `test human verification returned`() = runTest {
        webServer.prepareResponse(
            422,
            humanVerificationResponse
        )
        val humanVerificationDetails = spyk(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = mockk(),
                verificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery { humanVerificationProvider.getHumanVerificationDetails(clientId) } returns humanVerificationDetails
        coEvery { deviceVerificationProvider.getSolvedChallenge(session.sessionId) } returns null

        val result = backend(ApiManager.Call(0) { test() })
        assertTrue(result is ApiResult.Error.Http)
        val data = result.proton
        assertNotNull(data)
        val humanVerification = data.humanVerification
        assertNotNull(humanVerification)
        assertNotNull(humanVerification.verificationToken)
        assertEquals(3, humanVerification.verificationMethods.size)
        assertTrue(VerificationMethod.CAPTCHA.equalsNoCase(humanVerification.verificationMethods[0]))
    }

    @Test
    fun `test human verification for cookie returned`() = runTest {
        val clientId = MockClientId.getForCookie(CookieSessionId("test-cookie-id"))
        every { cookieJar.loadForRequest(any()) } returns listOf(testSessionCookie())
        coEvery { clientIdProvider.getClientId(any()) } returns clientId

        val backend = createBackend(null) {
            testTlsHelper.initPinning(it, TestTLSHelper.TEST_PINS)
        }

        coEvery { deviceVerificationProvider.getSolvedChallenge(null) } returns null
        webServer.prepareResponse(
            422,
            humanVerificationResponse
        )
        val humanVerificationDetails = spyk(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = mockk(),
                verificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery { humanVerificationProvider.getHumanVerificationDetails(clientId) } returns humanVerificationDetails

        val result = backend(ApiManager.Call(0) { test() })
        assertTrue(result is ApiResult.Error.Http)
        val data = result.proton
        assertNotNull(data)
        val humanVerification = data.humanVerification
        assertNotNull(humanVerification)
        assertNotNull(humanVerification.verificationToken)
        assertEquals(3, humanVerification.verificationMethods.size)
        assertTrue(VerificationMethod.CAPTCHA.equalsNoCase(humanVerification.verificationMethods[0]))
    }

    @Test
    fun `test other details returned`() = runTest {
        webServer.prepareResponse(
            422,
            otherDetailsResponse
        )
        val humanVerificationDetails = spyk(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = mockk(),
                verificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery { humanVerificationProvider.getHumanVerificationDetails(clientId) } returns humanVerificationDetails
        coEvery { deviceVerificationProvider.getSolvedChallenge(session.sessionId) } returns null

        val result = backend(ApiManager.Call(0) { test() })
        assertTrue(result is ApiResult.Error.Http)
        val data = result.proton
        assertNotNull(data)
        assertNull(data.humanVerification)
    }

    @Test
    fun `test human verification headers for sessionId`() = runTest {
        webServer.prepareResponse(
            422,
            humanVerificationResponse
        )
        val humanVerificationDetails = spyk(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = mockk(),
                verificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery { humanVerificationProvider.getHumanVerificationDetails(clientId) } returns humanVerificationDetails
        coEvery { deviceVerificationProvider.getSolvedChallenge(session.sessionId) } returns null

        backend(ApiManager.Call(0) { test() })
        val headers = webServer.takeRequestWithDefaultTimeout()?.headers
        verify(exactly = 1) {
            humanVerificationDetails.tokenCode
        }
        verify(exactly = 1) {
            humanVerificationDetails.tokenType
        }
        assertTrue(headers?.contains(Pair("x-pm-human-verification-token-type", "captcha")) ?: false)
        assertTrue(headers?.contains(Pair("x-pm-human-verification-token", "captcha token")) ?: false)
    }

    @Test
    fun `test human verification headers for cookieId`() = runTest {
        val clientId = MockClientId.getForCookie(CookieSessionId("test-cookie-id"))
        every { cookieJar.loadForRequest(any()) } returns listOf(testSessionCookie())
        coEvery { clientIdProvider.getClientId(any()) } returns clientId

        val backend = createBackend(null) {
            testTlsHelper.initPinning(it, TestTLSHelper.TEST_PINS)
        }

        webServer.prepareResponse(
            422,
            humanVerificationResponse
        )
        val humanVerificationDetails = spyk(
            HumanVerificationDetails(
                clientId = clientId,
                verificationMethods = mockk(),
                verificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery { humanVerificationProvider.getHumanVerificationDetails(clientId) } returns humanVerificationDetails
        coEvery { deviceVerificationProvider.getSolvedChallenge(null) } returns null

        backend(ApiManager.Call(0) { test() })
        val headers = webServer.takeRequestWithDefaultTimeout()?.headers
        verify(exactly = 1) {
            humanVerificationDetails.tokenCode
        }
        verify(exactly = 1) {
            humanVerificationDetails.tokenType
        }
        assertTrue(headers?.contains(Pair("x-pm-human-verification-token-type", "captcha")) ?: false)
        assertTrue(headers?.contains(Pair("x-pm-human-verification-token", "captcha token")) ?: false)
    }

    private fun testSessionCookie(): Cookie =
        Cookie.Builder().name("Session-Id").value("test-cookie-id").domain(webServer.hostName).build()
}
