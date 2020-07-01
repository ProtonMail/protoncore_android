@file:Suppress("PrivatePropertyName")

package ch.protonmail.libs.api

import me.proton.android.core.data.api.interceptor.ProtonInterceptor
import me.proton.android.core.data.api.entity.response.ApiErrorResponseBody
import ch.protonmail.libs.core.utils.EMPTY_STRING
import ch.protonmail.libs.testKotlin.`run only on Java 1_8-242`
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.serialization.json.json
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for [ProtonInterceptor]
 * @author Davide Farella
 */
internal class ProtonInterceptorTest {

    private val CODE_400 = 400
    private val CODE_401 = 401

    private val SOME_ERROR = "some error"

    @Test
    fun `response is successfully if code is 200`() {
        val client = httpClient(protonInterceptor(responseCode = 200, responseBody = ""))
        val response = client.fakeCall()

        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
    }

    @Test
    fun `response is failed if code is not 200`() {
        val client = httpClient(protonInterceptor(responseCode = CODE_401, responseBody = ""))
        val response = client.fakeCall()

        assertFalse(response.isSuccessful)
        assertEquals(CODE_401, response.code())
    }

    @Test
    fun `response is failed if code is 200, but body contains a failure code`() = `run only on Java 1_8-242` {
        val details = mapOf(
            "one" to "1",
            "two" to "two"
        )
        val errorBody = json {
            "Code" to CODE_400
            "Error" to SOME_ERROR
            "Details" to json {
                for ((k, v) in details) k to v
            }
        }.toString()

        val client = httpClient(protonInterceptor(responseCode = 200, responseBody = errorBody))
        val response = client.fakeCall()

        assertFalse(response.isSuccessful)
        assertEquals(CODE_400, response.code())
        assertEquals(SOME_ERROR, response.message())

        val body = response.body()
        assert(body is ApiErrorResponseBody)
        val errorResponseBody = body as ApiErrorResponseBody
        assertEquals(details, errorResponseBody.details)
    }
}

private fun OkHttpClient.fakeCall() = newCall(FakeTestEnvironment.request).execute()

private fun protonInterceptor(responseCode: Int, responseBody: String) =
    FakeTestEnvironment.buildRealProtonInterceptor(responseCode, responseBody)

private fun httpClient(interceptor: ProtonInterceptor) = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

/**
 * A fake test environment that returns fake [Response] with [Response.code] and [Response.body]
 * based on our needing
 *
 * Do not call this any field or function from this object from [ProtonInterceptorTest], they must
 * be called only from the helper functions listed above here
 */
private object FakeTestEnvironment {
    val request: Request = Request.Builder().url("http://fake.website").build()
    private val jsonMediaType = MediaType.parse("application/json; charset=utf-8")



    fun buildRealProtonInterceptor(responseCode: Int, responseBody: String) =
        spyk(
            ProtonInterceptor(
                mockk(),
                mockk(),
                "1.0",
                "Proton-Android",
                "en",
                mockk()
            )
        ) {
            // Real `ProtonInterceptor` for handle a real call with a fake chain
            val underlying =
                ProtonInterceptor(
                    mockk(),
                    mockk(),
                    "1.0",
                    "Proton-Android",
                    "en",
                    mockk()
                )

            every { intercept(any()) } answers {
                underlying.intercept(chain(responseCode, responseBody))
            }
        }

    private fun chain(responseCode: Int, responseBody: String) =
        mockk<Interceptor.Chain>(relaxed = true) {
            val response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .message(EMPTY_STRING)
                .code(responseCode)
                .body(ResponseBody.create(jsonMediaType, responseBody))
                .build()

            every { request() } returns request
            every { proceed(request) } returns response
        }
}
