@file:Suppress("EXPERIMENTAL_API_USAGE", "ClassName") // Coroutines test

package ch.protonmail.libs.core

import androidx.test.platform.app.InstrumentationRegistry
import ch.protonmail.libs.core.connection.NetworkManager
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for [NetworkManager]
 * See also instrumented test `NetworkMangerAndroidTest`
 *
 * @author Davide Farella
 */
internal class NetworkManagerTest {
    private val networkManager =
        NetworkManager(
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

    @Test
    fun `register throws exception when is called more than once`() {
        assertFailsWith<IllegalStateException> {
            with(networkManager) {
                register()
                register()
            }
        }
    }

    @Test
    fun `unregister unregister correctly`() {
        with(networkManager) {
            register()
            assertTrue(isRegistered())
            unregister()
            assertFalse(isRegistered())
        }
    }

    @Test
    fun `observe register correctly when no registered`() {
        with(networkManager) {
            assertFalse(isRegistered())
            observe {  }
            assertTrue(isRegistered())
        }
    }

    @Test
    fun `observe does not register again when already registered`() {
        with(networkManager) {
            register()
            assertTrue(isRegistered())
            observe {  }
            assertTrue(isRegistered())
        }
    }

    @Test
    fun `observe Flow register and unregister correctly`() {
        with(networkManager) {
            // Assert is NOT registered
            assertFalse(isRegistered())
            try {
                runBlockingTest {
                    launch { observe().collect() }
                    advanceUntilIdle()
                    // Assert IS registered
                    assertTrue(isRegistered())
                    cancel()
                }
            } catch (e: CancellationException) {}
            // Assert is NOT registered
            assertFalse(isRegistered())
        }
    }
}

// TODO: remove ignore on Robolectric 4.3.2
@Ignore("Robolectric bug: https://github.com/robolectric/robolectric/issues/5303")
@RunWith(RobolectricTestRunner::class)
internal class NetworkManager_Android_Test {
    @Test
    fun `initialize correctly`() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(NetworkManager(context).canUseNetwork())
    }
}
