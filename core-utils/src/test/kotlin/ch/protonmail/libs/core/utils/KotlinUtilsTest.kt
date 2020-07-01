@file:Suppress("EXPERIMENTAL_API_USAGE")

package ch.protonmail.libs.core.utils

import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

/**
 * Test suite for `KotlinUtils` file
 * @author Davide Farella
 */
internal class KotlinUtilsTest {

    @Test
    fun `await works correctly`() = runBlocking {
        var predicate = false
        val capture = mockk<(Int) -> Unit>(relaxed = true)

        launch {
            capture(0) // Verify that this coroutine is launched instantly
            await { predicate }
            capture(2)
        }
        delay(10) // wait for `launch` builder to be completed
        capture(1)
        predicate = true
        delay(100) // wait for `await` to return
        capture(3)

        verifyOrder {
            capture(0)
            capture(1)
            capture(2)
            capture(3)
        }
    }
}
