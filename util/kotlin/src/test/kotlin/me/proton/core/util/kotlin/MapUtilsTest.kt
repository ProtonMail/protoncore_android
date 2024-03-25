package me.proton.core.util.kotlin

import org.junit.Assert.assertEquals
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

        val filteredMap = map.filterNotNullValues()
        assertEquals(3, filteredMap.size)
    }

}
