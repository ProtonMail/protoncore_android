package me.proton.core.test.quark.v2

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import me.proton.core.test.quark.v2.extension.isReady
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

class QuarkCommandTests {

    private val quarkCommand = QuarkCommand()

    @Before
    fun setup() {
        mockkObject(quarkCommand)
        every { quarkCommand.client } returns mockk()

        quarkCommand.route("test/route")
            .baseUrl("https://test.com")
            .proxyToken("testToken")
            .httpClientTimeout(10.seconds)
    }

    @Test
    fun testBuildRequest() {
        quarkCommand
            .build()
            .apply {
                assertEquals("https://test.com/test/route?", url.toString())
                assertEquals("testToken", header("x-atlas-secret"))
            }
    }

    @Test
    fun testExecuteRequest() {
        fun QuarkCommand.test(statusCode: Int) {
            val response = mockResponse(statusCode)
            quarkCommand
                .onResponse({}, { false })
                .client
                .executeQuarkRequest(response.request)
                .apply {
                    assertEquals(statusCode, code)
                }
        }

        arrayOf(200, 503, 404).forEach {
            quarkCommand.test(it)
        }
    }

    @Test
    fun testToEncodedArgs() {
        val encodedArgs = listOf("key1" to "value1", "key2" to "value2").toEncodedArgs()
        val expected = arrayOf(
            "key1=${URLEncoder.encode("value1", "UTF-8")}",
            "key2=${URLEncoder.encode("value2", "UTF-8")}"
        )
        assertArrayEquals(expected, encodedArgs)
    }

    @Test
    fun testBuilderPropertiesNotPreserved() {
        quarkCommand.route("route1")
            .baseUrl("https://example1.com")
            .proxyToken("token1")
            .httpClientTimeout(5.seconds)

        val request1 = quarkCommand.build()

        quarkCommand.route("route2")
            .baseUrl("https://example2.com")
            .proxyToken("token2")
            .httpClientTimeout(10.seconds)

        val request2 = quarkCommand.build()

        // Check that properties from the first call are not preserved in the second call
        assertEquals("https://example1.com/route1?", request1.url.toString())
        assertEquals("token1", request1.header("x-atlas-secret"))
        assertNotEquals(request1.url, request2.url)
        assertNotEquals(request1.header("x-atlas-secret"), request2.header("x-atlas-secret"))
    }

    @Test
    fun testCustomRequestBuilder() {
        quarkCommand
            .onRequestBuilder { header("Custom-Header", "CustomValue") }
            .build()
            .apply {
                assertEquals("CustomValue", header("Custom-Header"))
            }
    }

    @Test
    fun testIsReadyExtension() {
        mockResponse(200)
        assertTrue(quarkCommand.isReady())

        mockResponse(500)
        assertFalse(quarkCommand.onResponse({}, { true }).isReady())
    }

    private fun mockResponse(statusCode: Int): Response {
        val mockResponse: Response = mockk {
            every { code } returns statusCode
            every { message } returns ""
            every { body } returns mockk {
                every { string() } returns "{}"
            }
            every { request } returns Request.Builder().url("https://test.com/test/route").build()
        }

        every { quarkCommand.client } returns mockk {
            every { newCall(any()).execute() } returns mockResponse
        }

        return mockResponse
    }
}
