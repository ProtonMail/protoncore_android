/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.text.SimpleDateFormat
import java.util.Date

/** Dispatcher for [okhttp3.mockwebserver.MockWebServer].
 * By default, it will try to find and return the contents of a file,
 * that correspond to the request path. For example, for request
 * `GET /auth/modulus`, it will load a file from assets directory: `GET/auth/modulus.json`.
 * If a file doesn't exist, it'll return 404.
 * If you don't want the default behavior, you can override the response
 * by calling [mockFromAssets] or [mock].
 */
class TestWebServerDispatcher : Dispatcher() {
    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    private val responses = mutableMapOf<MockedEndpoint, MutableList<MockResponse>>()

    override fun dispatch(request: RecordedRequest): MockResponse {
        val mockResponses = responses[MockedEndpoint(request.method!!, request.requestUrl?.encodedPath!!)]
        val queuedResponse = if (mockResponses != null && mockResponses.size > 1) {
            mockResponses.removeFirstOrNull()
        } else {
            mockResponses?.first()
        }
        val mockResponse = queuedResponse ?: kotlin.runCatching {
            val body = loadFromAssets("${request.method}${request.requestUrl?.encodedPath}.json")
            MockResponse().setResponseCode(200).setBody(body)
        }.getOrNull()

        val response = mockResponse?.applyDefaultHeaders()
        Log.d("TestWebServerDispatcher", "Request $request")

        return if (response != null) {
            Log.d("TestWebServerDispatcher", "Response $response")
            response
        } else {
            Log.e("TestWebServerDispatcher", "Response not found for request $request")
            MockResponse().setResponseCode(404)
        }
    }

    private fun MockResponse.applyDefaultHeaders(): MockResponse =
        addHeader("Set-Cookie", "Session-Id=test-session-id")
            .addHeader("Date", dateFormat.format(Date()))

    /** Mocks a response for a request.
     * You can call this method multiple times, even with the same [requestMethod] and [requestPath].
     * In that case, the responses will be queued. Once they are served, they are removed,
     * so the next response for the same request will be served next.
     * @param requestMethod The HTTP method name ("GET", "POST" etc.)
     * @param requestPath The path of the incoming request, starting with `/`.
     * @param responsePath The path of the asset file which will be used as a response body.
     * @param responseCode The HTTP code for the response.
     */
    fun mockFromAssets(
        requestMethod: String,
        requestPath: String,
        responsePath: String,
        responseCode: Int = 200
    ) {
        val body = loadFromAssets(responsePath)
        val response = MockResponse().setResponseCode(responseCode).setBody(body)
        mock(
            requestMethod = requestMethod,
            requestPath = requestPath,
            mockResponse = response
        )
    }

    fun mock(requestMethod: String, requestPath: String, mockResponse: MockResponse) {
        val key = MockedEndpoint(method = requestMethod, path = requestPath)
        val queuedResponses = responses[key] ?: mutableListOf<MockResponse>().also {
            responses[key] = it
        }
        queuedResponses.add(mockResponse)
    }

    private fun loadFromAssets(path: String): String {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        return assetManager.open(path).bufferedReader().use { it.readText() }
    }

    private data class MockedEndpoint(
        val method: String,
        val path: String
    )
}
