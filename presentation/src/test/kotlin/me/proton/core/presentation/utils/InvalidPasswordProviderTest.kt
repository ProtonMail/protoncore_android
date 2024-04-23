package me.proton.core.presentation.utils

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InvalidPasswordProviderTest {
    @MockK
    private lateinit var context: Context

    private lateinit var tested: InvalidPasswordProvider

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = InvalidPasswordProvider(context)
    }

    @Test
    fun `empty passwords file`() = runTest {
        mockCommonPasswordAssets("")

        tested.init()
        assertFalse(tested.isPasswordCommon("password"))
    }

    @Test
    fun `passwords not initialized`() = runTest {
        mockCommonPasswordAssets("password")

        // Call to `tested.init()` skipped
        assertFalse(tested.isPasswordCommon("password"))
    }

    @Test
    fun `single password`() = runTest {
        mockCommonPasswordAssets("password")

        tested.init()
        assertTrue(tested.isPasswordCommon("password"))
    }

    @Test
    fun `multiple passwords`() = runTest {
        mockCommonPasswordAssets("password\n\n    \nqwerty\n  asdf\n")

        tested.init()
        assertTrue(tested.isPasswordCommon("password"))
        assertTrue(tested.isPasswordCommon("qwerty"))
        assertTrue(tested.isPasswordCommon("asdf"))
    }

    private fun mockCommonPasswordAssets(contents: String) {
        every { context.assets } returns mockk {
            every { open(any()) } returns ByteArrayInputStream(contents.toByteArray())
        }
    }
}
