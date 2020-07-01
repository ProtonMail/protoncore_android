package ch.protonmail.libs.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

/**
 * Test suite for Collection utils
 * @author Davide Farella
 */
internal class CollectionUtilsTest {

    // region forEachAsync
    @Test
    fun `forEachAsync executes concurrently`() {
        val task = suspend { delay(100) }
        val tasks = (1..10).map { task }

        val asyncTime = runBlocking {
            measureTimeMillis { tasks.forEachAsync { it() } }
        }

        assert(asyncTime in 100..200)
    }

    @Test
    fun `forEachAsync works correctly`() {
        val i = AtomicInteger()
        val increment = { i.incrementAndGet() }
        val increments = (1..10).map { increment }

        runBlocking { increments.forEachAsync { it() } }

        assertEquals(10, i.get())
    }
    // endregion

    // region mapAsync
    @Test
    fun `mapAsync executes concurrently`() {
        val task = suspend { delay(100) }
        val tasks = (1..10).map { task }

        val asyncTime = runBlocking {
            measureTimeMillis { tasks.mapAsync { it() } }
        }

        assert(asyncTime in 100..200)
    }

    @Test
    fun `mapAsync works correctly`() {
        val input = (1..10)
        // Multiply the input * 10
        val mapper: suspend (Int) -> Int = { it * 10 }

        val output = runBlocking { input.mapAsync(mapper) }

        // toList casting is required for match the assertion
        assertEquals((10..100 step 10).toList(), output.toList())
    }
    // endregion

    // region Map
    @Test
    fun `Map filterValues by java class`() {
        val mixedMap = mapOf(
            "1" to 1,
            "2" to 2,
            "3" to false,
            "4" to "hello"
        )
        val result = mixedMap.filterValues(String::class.java)
        assertEquals(1, result.size)
        assertEquals("hello", result.values.first())
    }

    @Test
    fun `Map filterValues by kotlin class`() {
        val mixedMap = mapOf(
            "1" to 1,
            "2" to 2,
            "3" to false,
            "4" to "hello"
        )
        val expected = mapOf("1" to 1, "2" to 2)
        val result = mixedMap.filterValues(Int::class)
        assertEquals(expected, result)
    }

    @Test
    fun `Map filterValues by reified`() {
        val mixedMap = mapOf(
            "1" to 1,
            "2" to 2,
            "3" to false,
            "4" to "hello"
        )
        val expected = mapOf("1" to 1, "2" to 2)
        val result = mixedMap.filterValues<String, Int>()
        assertEquals(expected, result)
    }
    // endregion
}
