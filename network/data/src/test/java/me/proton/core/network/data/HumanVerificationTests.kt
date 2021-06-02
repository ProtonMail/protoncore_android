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
import me.proton.core.network.data.di.ApiFactory
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
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.ClientId
import me.proton.core.network.domain.humanverification.CookieSessionId
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationState
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

    private val scope = CoroutineScope(TestCoroutineDispatcher())

    private val testTlsHelper = TestTLSHelper()
    private lateinit var apiFactory: ApiFactory
    private lateinit var webServer: MockWebServer

    private lateinit var backend: ProtonApiBackend<TestRetrofitApi>
    private lateinit var logger: MockLogger
    private lateinit var client: MockApiClient

    private lateinit var session: Session
    private lateinit var clientId: ClientId

    private var sessionProvider = mockk<SessionProvider>()
    private val humanVerificationProvider = mockk<HumanVerificationProvider>()
    private val humanVerificationListener = mockk<HumanVerificationListener>()

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
        coEvery { sessionProvider.getSessionId(any()) } returns session.sessionId
        coEvery { sessionProvider.getSession(any()) } returns session
        every { cookieStore.get(any()) } returns emptyList()

        apiFactory =
            ApiFactory(
                "https://example.com/",
                client,
                logger,
                networkManager,
                prefs,
                sessionProvider,
                humanVerificationProvider,
                sessionListener,
                humanVerificationListener,
                cookieStore,
                scope
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
            logger,
            sessionId,
            sessionProvider,
            humanVerificationProvider,
            cookieStore,
            apiFactory.baseOkHttpClient,
            listOf(
                ScalarsConverterFactory.create(),
                apiFactory.jsonConverter
            ),
            TestRetrofitApi::class,
            networkManager,
            pinningInit
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
                captchaVerificationToken = null,
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
        assertNotNull(humanVerification.captchaVerificationToken)
        assertEquals(3, humanVerification.verificationMethods.size)
        assertTrue("captcha".equalsNoCase(humanVerification.verificationMethods[0].name))
    }

    @Test
    fun `test human verification for cookie returned`() = runBlocking {
        every { cookieStore.get(any()) } returns listOf(HttpCookie("Session-Id", "test-cookie-id"))

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
                captchaVerificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery {
            humanVerificationProvider.getHumanVerificationDetails(
                ClientId.CookieSession(
                    CookieSessionId(
                        "test-cookie-id"
                    )
                )
            )
        } returns humanVerificationDetails

        val result = backend(ApiManager.Call(0) { test() })
        assertTrue(result is ApiResult.Error.Http)
        val data = result.proton
        assertNotNull(data)
        val humanVerification = data.humanVerification
        assertNotNull(humanVerification)
        assertNotNull(humanVerification.captchaVerificationToken)
        assertEquals(3, humanVerification.verificationMethods.size)
        assertTrue("captcha".equalsNoCase(humanVerification.verificationMethods[0].name))
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
                captchaVerificationToken = null,
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
                captchaVerificationToken = null,
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
        every { cookieStore.get(any()) } returns listOf(HttpCookie("Session-Id", "test-cookie-id"))

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
                captchaVerificationToken = null,
                state = HumanVerificationState.HumanVerificationSuccess,
                tokenType = "captcha",
                tokenCode = "captcha token"
            )
        )

        coEvery {
            humanVerificationProvider.getHumanVerificationDetails(
                ClientId.CookieSession(
                    CookieSessionId(
                        "test-cookie-id"
                    )
                )
            )
        } returns humanVerificationDetails

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
