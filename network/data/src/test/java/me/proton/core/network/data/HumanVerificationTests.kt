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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import me.proton.core.network.data.util.MockApiClient
import me.proton.core.network.data.util.MockClientId
import me.proton.core.network.data.util.MockLogger
import me.proton.core.network.data.util.MockNetworkPrefs
import me.proton.core.network.data.util.MockSession
import me.proton.core.network.data.util.MockSessionListener
import me.proton.core.network.data.util.TestRetrofitApi
import me.proton.core.network.data.util.TestTLSHelper
import me.proton.core.network.data.util.prepareResponse
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.CookieSessionId
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
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.HttpCookie
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
                "Code": 9002,
                "Error": "Something other happened",
                "ErrorDescription": "",
                "Details": {
                    "SomethingOther" : "something other"
                }
            }
        """.trimIndent()

    private fun javaWallClockMs(): Long = System.currentTimeMillis()

    private val scope = CoroutineScope(TestCoroutineDispatcher())

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
    private val missingScopeListener = mockk<MissingScopeListener>(relaxed = true)

    private var sessionListener: SessionListener = MockSessionListener(
        onTokenRefreshed = { session -> this.session = session }
    )
    private val cookieStore = mockk<ProtonCookieStore>()

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
        every { clientIdProvider.getClientId(any()) } returns clientId

        coEvery { sessionProvider.getSessionId(any()) } returns session.sessionId
        coEvery { sessionProvider.getSession(any()) } returns session
        every { cookieStore.get(any()) } returns emptyList()

        apiManagerFactory =
            ApiManagerFactory(
                "https://example.com/",
                client,
                clientIdProvider,
                serverTimeListener,
                networkManager,
                prefs,
                sessionProvider,
                sessionListener,
                humanVerificationProvider,
                humanVerificationListener,
                missingScopeListener,
                cookieStore,
                scope,
                cache = { null },
                apiConnectionListener = null
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
            { apiManagerFactory.baseOkHttpClient } ,
            listOf(
                ScalarsConverterFactory.create(),
                apiManagerFactory.jsonConverter
            ),
            TestRetrofitApi::class,
            networkManager,
            pinningInit,
            ::javaWallClockMs
        )

    @After
    fun after() {
        webServer.shutdown()
    }

    @Test
    fun `test human verification returned`() = runBlocking {
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
    fun `test human verification for cookie returned`() = runBlocking {
        val clientId = MockClientId.getForCookie(CookieSessionId("test-cookie-id"))
        every { cookieStore.get(any()) } returns listOf(HttpCookie("Session-Id", "test-cookie-id"))
        every { clientIdProvider.getClientId(any()) } returns clientId

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
    fun `test other details returned`() = runBlocking {
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
        val result = backend(ApiManager.Call(0) { test() })
        assertTrue(result is ApiResult.Error.Http)
        val data = result.proton
        assertNotNull(data)
        assertNull(data.humanVerification)
    }

    @Test
    fun `test human verification headers for sessionId`() = runBlocking {
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

        backend(ApiManager.Call(0) { test() })
        val headers = webServer.takeRequest().headers
        verify(exactly = 1) {
            humanVerificationDetails.tokenCode
        }
        verify(exactly = 1) {
            humanVerificationDetails.tokenType
        }
        assertTrue(headers.contains(Pair("x-pm-human-verification-token-type", "captcha")))
        assertTrue(headers.contains(Pair("x-pm-human-verification-token", "captcha token")))
    }

    @Test
    fun `test human verification headers for cookieId`() = runBlocking {
        val clientId = MockClientId.getForCookie(CookieSessionId("test-cookie-id"))
        every { cookieStore.get(any()) } returns listOf(HttpCookie("Session-Id", "test-cookie-id"))
        every { clientIdProvider.getClientId(any()) } returns clientId

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

        backend(ApiManager.Call(0) { test() })
        val headers = webServer.takeRequest().headers
        verify(exactly = 1) {
            humanVerificationDetails.tokenCode
        }
        verify(exactly = 1) {
            humanVerificationDetails.tokenType
        }
        assertTrue(headers.contains(Pair("x-pm-human-verification-token-type", "captcha")))
        assertTrue(headers.contains(Pair("x-pm-human-verification-token", "captcha token")))
    }
}
