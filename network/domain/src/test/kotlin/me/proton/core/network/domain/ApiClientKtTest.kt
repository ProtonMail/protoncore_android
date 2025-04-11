package me.proton.core.network.domain

import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiClientKtTest {
    private lateinit var tested: ApiClient

    @BeforeTest
    fun setUp() {
        tested = mockk()
    }

    @Test
    fun `should return client id`() {
        every { tested.appVersionHeader } returns "android-mail@1.2.3"
        assertEquals("android-mail", tested.applicationName)
    }

    @Test
    fun `should return whole string if version suffix is missing`() {
        every { tested.appVersionHeader } returns "android-mail"
        assertEquals("android-mail", tested.applicationName)
    }

    @Test
    fun `should return the first token`() {
        every { tested.appVersionHeader } returns "android-mail@1.2.3@0"
        assertEquals("android-mail", tested.applicationName)
    }
}
