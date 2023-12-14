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

package me.proton.core.humanverification.presentation.ui.common

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.ui.common.HumanVerificationWebViewClient.Companion.isIpAddress
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HumanVerificationWebViewClientTest {
    private val altUrl = "http://alt_api_host"
    private val apiHost = "api_host"
    private val verifyAppHost = "hv_host"
    private val verifyAppUrl = "http://$verifyAppHost"

    private lateinit var extraHeaderProvider: ExtraHeaderProvider

    @MockK
    private lateinit var networkPrefs: NetworkPrefs

    @MockK
    private lateinit var networkRequestOverrider: NetworkRequestOverrider

    @MockK(relaxed = true)
    lateinit var onResourceLoadingError: (WebResourceRequest?, WebResponseError?) -> Unit

    private lateinit var tested: HumanVerificationWebViewClient

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(CookieManager::class)
        extraHeaderProvider = ExtraHeaderProviderImpl()
        tested = HumanVerificationWebViewClient(
            apiHost,
            extraHeaderProvider,
            networkPrefs,
            networkRequestOverrider,
            onResourceLoadingError,
            verifyAppUrl
        )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `do not override POST request`() {
        // GIVEN
        val request = mockk<WebResourceRequest> {
            every { method } returns "POST"
            every { url } returns mockk()
        }

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNull(response)
    }

    @Test
    fun `do not override GET request with root domain but no extra headers`() {
        // GIVEN
        val request = mockk<WebResourceRequest> {
            every { method } returns "GET"
            every { url } returns Uri.parse("http://$apiHost/")
        }
        every { networkPrefs.activeAltBaseUrl } returns null

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNull(response)
    }

    @Test
    fun `do not override GET request with non-root domain and extra headers`() {
        // GIVEN
        val request = mockk<WebResourceRequest> {
            every { method } returns "GET"
            every { url } returns Uri.parse("http://third_party_host/resource")
        }
        every { networkPrefs.activeAltBaseUrl } returns null
        extraHeaderProvider.addHeaders("x-extra" to "extra_value")

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNull(response)
    }

    @Test
    fun `override GET request with root domain and extra headers`() {
        // GIVEN
        val cookieManager = mockk<CookieManager>(relaxed = true)
        val setCookieHeaderValue = "key=value"
        val requestUrl = "http://${apiHost}/index.html"
        val request = mockk<WebResourceRequest> {
            every { requestHeaders } returns emptyMap()
            every { method } returns "GET"
            every { url } returns Uri.parse(requestUrl)
        }

        extraHeaderProvider.addHeaders("x-extra" to "value")

        every {
            networkRequestOverrider.overrideRequest(any(), any(), any(), any(), any(), any())
        } returns NetworkRequestOverrider.Result(
            mimeType = null,
            encoding = null,
            contents = null,
            httpStatusCode = 200,
            reasonPhrase = "OK",
            responseHeaders = mapOf("Set-Cookie" to setCookieHeaderValue),
        )

        every { CookieManager.getInstance() } returns cookieManager
        every { networkPrefs.activeAltBaseUrl } returns null

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNotNull(response)
        verify { cookieManager.setCookie(requestUrl, setCookieHeaderValue) }
        verify {
            networkRequestOverrider.overrideRequest(
                requestUrl,
                "GET",
                listOf("x-extra" to "value")
            )
        }
    }

    @Test
    fun `override GET request with alt host and extra headers`() {
        // GIVEN
        val requestUrl = "${altUrl}/index.html"
        val request = mockk<WebResourceRequest> {
            every { requestHeaders } returns emptyMap()
            every { method } returns "GET"
            every { url } returns Uri.parse(requestUrl)
        }

        extraHeaderProvider.addHeaders("x-extra" to "value")

        every {
            networkRequestOverrider.overrideRequest(any(), any(), any(), any(), any(), any())
        } returns NetworkRequestOverrider.Result(
            mimeType = null,
            encoding = null,
            contents = null,
            httpStatusCode = 200,
            reasonPhrase = "OK",
            responseHeaders = mapOf(
                "content-security-policy" to "csp_value",
                "set-cookie" to "key=value"
            ),
        )

        every { CookieManager.getInstance() } returns mockk(relaxed = true)
        every { networkPrefs.activeAltBaseUrl } returns altUrl

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNotNull(response)
        assertEquals(
            mapOf("set-cookie" to "key=value"), // Note: no CSP header
            response.responseHeaders
        )

        val responseHeadersSlot = slot<List<Pair<String, String>>>()
        verify {
            networkRequestOverrider.overrideRequest(
                requestUrl,
                "GET",
                capture(responseHeadersSlot),
                acceptSelfSignedCertificates = true,
                body = null, bodyType = null
            )
        }
        assertEquals(
            mapOf("x-extra" to "value", "X-PM-DoH-Host" to verifyAppHost),
            responseHeadersSlot.captured.toMap()
        )
    }

    @Test
    fun `do not add DoH-Host header when loading captcha`() {
        // GIVEN
        val requestUrl = "${altUrl}/core/v4/captcha"
        val request = mockk<WebResourceRequest> {
            every { requestHeaders } returns emptyMap()
            every { method } returns "GET"
            every { url } returns Uri.parse(requestUrl)
        }

        every {
            networkRequestOverrider.overrideRequest(any(), any(), any(), any(), any(), any())
        } returns NetworkRequestOverrider.Result(
            mimeType = null,
            encoding = null,
            contents = null,
            httpStatusCode = 200,
            reasonPhrase = "OK",
            responseHeaders = mapOf(),
        )

        every { CookieManager.getInstance() } returns mockk(relaxed = true)
        every { networkPrefs.activeAltBaseUrl } returns altUrl

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNotNull(response)
        assertTrue(response.responseHeaders.isEmpty())

        val responseHeadersSlot = slot<List<Pair<String, String>>>()
        verify {
            networkRequestOverrider.overrideRequest(
                requestUrl,
                "GET",
                capture(responseHeadersSlot),
                acceptSelfSignedCertificates = true,
                body = null, bodyType = null
            )
        }
        assertTrue(responseHeadersSlot.captured.isEmpty())
    }

    @Test
    fun `do not add DoH-Host header when loading API url`() {
        // GIVEN
        val requestUrl = "${altUrl}/api/core/v4/verification/ownership-email/5uDQxS"
        val request = mockk<WebResourceRequest> {
            every { requestHeaders } returns emptyMap()
            every { method } returns "GET"
            every { url } returns Uri.parse(requestUrl)
        }

        every {
            networkRequestOverrider.overrideRequest(any(), any(), any(), any(), any(), any())
        } returns NetworkRequestOverrider.Result(
            mimeType = null,
            encoding = null,
            contents = null,
            httpStatusCode = 200,
            reasonPhrase = "OK",
            responseHeaders = mapOf(),
        )

        every { CookieManager.getInstance() } returns mockk(relaxed = true)
        every { networkPrefs.activeAltBaseUrl } returns altUrl

        // WHEN
        val response = tested.shouldInterceptRequest(mockk(), request)

        // THEN
        assertNotNull(response)
        assertTrue(response.responseHeaders.isEmpty())

        val responseHeadersSlot = slot<List<Pair<String, String>>>()
        verify {
            networkRequestOverrider.overrideRequest(
                requestUrl,
                "GET",
                capture(responseHeadersSlot),
                acceptSelfSignedCertificates = true,
                body = null, bodyType = null
            )
        }
        assertTrue(responseHeadersSlot.captured.isEmpty())
    }

    @Test
    fun `http error`() {
        // GIVEN
        val request = mockk<WebResourceRequest> {
            every { method } returns "GET"
            every { url } returns mockk()
        }
        val response = mockk<WebResourceResponse> {
            every { statusCode } returns 500
            every { reasonPhrase } returns "Server error"
        }

        // WHEN
        tested.onReceivedHttpError(mockk(), request, response)

        // THEN
        verify { onResourceLoadingError(request, WebResponseError.Http(response)) }
    }

    @Test
    fun `generic error`() {
        // GIVEN
        val request = mockk<WebResourceRequest> {
            every { isForMainFrame } returns true
            every { method } returns "GET"
            every { url } returns mockk()
        }
        val error = mockk<WebResourceError> {
            every { description } returns "description"
            every { errorCode } returns WebViewClient.ERROR_CONNECT
        }

        // WHEN
        tested.onReceivedError(mockk(), request, error)

        // THEN
        verify { onResourceLoadingError(request, WebResponseError.Resource(error)) }
    }

    @Test
    fun `IP addresses`() {
        assertFalse("".isIpAddress())
        assertFalse("api_host".isIpAddress())
        assertFalse("a.b.c.d".isIpAddress())
        assertFalse("abc".isIpAddress())
        assertFalse("0".isIpAddress())
        assertFalse("0.0".isIpAddress())
        assertFalse("0.0.0".isIpAddress())
        assertFalse("0.0.0.".isIpAddress())
        assertFalse("0.0.0 0".isIpAddress())
        assertFalse("0.0.0 0".isIpAddress())
        assertFalse("::".isIpAddress()) // Note: technically it's an unspecified address
        assertFalse("100:".isIpAddress())
        assertFalse(":100".isIpAddress())
        assertFalse("0:0:0:0:0:ffff:192,1,19,185".isIpAddress())

        assertTrue("0.0.0.0".isIpAddress())
        assertTrue("1.4.16.64".isIpAddress())
        assertTrue("255.255.255.255".isIpAddress())
        assertTrue("512.512.512.512".isIpAddress()) // Our impl doesn't check for range

        assertTrue("::1".isIpAddress())
        assertTrue("::ab".isIpAddress())
        assertTrue("ab::cd".isIpAddress())
        assertTrue("::ffff:0.0.0.0".isIpAddress())
        assertTrue("64:ff9b:1::".isIpAddress())
        assertTrue("100::".isIpAddress())
        assertTrue("ab::".isIpAddress())
        assertTrue("0000:0000:0000:0000:0000:0000:0000:0000".isIpAddress())
        assertTrue("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff".isIpAddress())
        assertTrue("4051:0:0:10:0:68f:300c:326b".isIpAddress())
        assertTrue("ca82:0:0:0:0:0:0:6d".isIpAddress())
        assertTrue("ca82::6d".isIpAddress())
        assertTrue("0:0:0:0:0:ffff:192.1.112.13".isIpAddress())
    }
}
