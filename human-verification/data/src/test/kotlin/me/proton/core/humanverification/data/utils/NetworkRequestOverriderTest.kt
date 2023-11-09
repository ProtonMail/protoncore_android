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

package me.proton.core.humanverification.data.utils

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetworkRequestOverriderTest {

    private lateinit var overrider: NetworkRequestOverriderImpl
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        overrider = NetworkRequestOverriderImpl(OkHttpClient())
        mockWebServer = MockWebServer().apply {
            enqueue(
                MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("Some response")
            )
        }
        mockkStatic(Base64::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Requests are created correctly`() {
        val headers = listOf("content-type" to "text/html")
        val contentType = "text/html; charset=utf-8"
        val bodyContents = "Some body".toByteArray()

        val getRequest = overrider.createRequest(
            "https://protonmail.com",
            "GET",
            headers
        )
        val postRequest = overrider.createRequest(
            "https://protonmail.com",
            "POST",
            headers,
            bodyContents.inputStream(),
            contentType
        )

        assertEquals("https://protonmail.com/", getRequest.url.toString())
        assertEquals("GET", getRequest.method)
        assertEquals(headers, getRequest.headers.map { it })
        assertNull(getRequest.body)

        assertEquals("https://protonmail.com/", postRequest.url.toString())
        assertEquals("POST", postRequest.method)
        assertEquals(headers, postRequest.headers.map { it })
        assertNotNull(postRequest.body)
        assertEquals(contentType.toMediaType(), postRequest.body?.contentType())
    }

    @Test
    fun `overrideRequest enqueues a request with the provided data and returns its response's contents`() {
        val url = mockWebServer.url("/").toUrl().toString()
        val headers = listOf("SOME_HEADER" to "SOME_VALUE")

        val result = overrider.overrideRequest(url, "GET", headers)
        val request = mockWebServer.takeRequest()

        assertTrue(request.headers.map { it }.contains(headers.first()))
        assertEquals("Some response", result.contents?.let { String(it.readBytes()) })
    }

    @Test
    fun `override request with insecure http client`() {
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }

        val url = mockWebServer.url("/").toUrl().toString()
        val headers = listOf("SOME_HEADER" to "SOME_VALUE")

        val result =
            overrider.overrideRequest(url, "GET", headers, acceptSelfSignedCertificates = true)
        val request = mockWebServer.takeRequest()

        assertTrue(request.headers.map { it }.contains(headers.first()))
        assertEquals("Some response", result.contents?.let { String(it.readBytes()) })
    }
}
