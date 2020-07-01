@file:Suppress("ClassName")

package ch.protonmail.libs.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * __Unit__ test suite for String utils
 * @author Davide Farella
 */
internal class StringUtilsTest {

    @Test
    fun stripEmptyLines() {
        val input = """
            |
            |Hello
            |
            |beautiful
            |
            |
            |
            |
            |  world
            |
            """.trimMargin("|")

        val e1 = """
            |
            |Hello
            |
            |beautiful
            |
            |
            |  world
            |
            """.trimMargin("|")
        assertEquals(e1, input.stripEmptyLines(allowed = 2, keepSurroundings = true))

        val e2 = """
            |Hello
            |
            |beautiful
            |
            |
            |world
            """.trimMargin("|")
        assertEquals(e2, input.stripEmptyLines(allowed = 2, map = { it.trim() }))

        val e3 = """
            |Hello
            |beautiful
            |world
            """.trimMargin("|")
        assertEquals(e3, input.stripEmptyLines(allowed = 0, map = { it.trim() }))
    }
}

/**
 * __Unit__ test suite for [ch.protonmail.libs.core.utils.substring]
 * @author Davide Farella
 */
internal class StringUtils_substring_Test {

    // GIVEN
    private val input = "Hello world what a beautiful day"

    @Test
    fun `substring match with inclusive and ignore case`() {
        val expected = "world what a beautiful"

        // WHEN
        val output = input.substring(
            "WORLD",
            "beautiful",
            startInclusive = true,
            endInclusive = true,
            ignoreCase = true,
            ignoreMissingStart = true,
            ignoreMissingEnd = true
        )

        // THEN
        assertEquals(expected, output)
    }

    @Test
    fun `substring match without inclusive`() {
        val expected = " what a "

        // WHEN
        val output = input.substring(
            "world",
            "beautiful",
            startInclusive = false,
            endInclusive = false
        )

        // THEN
        assertEquals(expected, output)
    }

    @Test
    fun `substring match with only start inclusive`() {
        val expected = "beautiful day"

        // WHEN
        val output = input.substring(
            "beautiful",
            startInclusive = true
        )

        // THEN
        assertEquals(expected, output)
    }

    @Test
    fun `substring not match if case is different`() {
        val unexpected = "Hello world"
        val expected = EMPTY_STRING

        // WHEN
        val output = input.substring(
            "HELLO",
            "WORLD"
        )

        // THEN
        assertNotEquals(unexpected, output)
        assertEquals(expected, output)
    }

    @Test
    fun `substring match with ignore missing start`() {
        val expected = "Hello world "

        // WHEN
        val output = input.substring(
            "Ciao",
            "what",
            ignoreMissingStart = true
        )

        // THEN
        assertEquals(expected, output)
    }

    @Test
    fun `substring match with ignore missing end`() {
        val expected = " a beautiful day"

        // WHEN
        val output = input.substring(
            "what",
            "banana",
            ignoreMissingEnd = true
        )

        // THEN
        assertEquals(expected, output)
    }

    @Test
    fun `substring match with ignore missing start and end`() {
        val expected = input

        // WHEN
        val output = input.substring(
            "ciao",
            "banana",
            ignoreMissingStart = true,
            ignoreMissingEnd = true
        )

        // THEN
        assertEquals(expected, output)
    }
}
