package ch.protonmail.libs.core.utils

import org.junit.Assert.*
import kotlin.test.Test

/**
 * Test suite for [Map] utils
 * @author Davide Farella
 */
internal class MapUtilsTest {

    @Test
    fun filterNullValues() {
        val map = mapOf(
            "1" to 1,
            "2" to 2,
            "null" to null,
            "3" to 3
        )
        assertEquals(4, map.size)

        val filteredMap = map.filterNullValues()
        assertEquals(3, filteredMap.size)
    }

}
