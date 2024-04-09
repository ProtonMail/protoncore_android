/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.configuration

import me.proton.core.configuration.provider.MapConfigFieldProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MapConfigFieldProviderTest {

    @Test
    fun `test getString returns correct String value`() {
        val map = mapOf("key1" to "value1")
        val provider = MapConfigFieldProvider(map)
        assertEquals("value1", provider.getString("key1"))
    }

    @Test
    fun `test getString returns null for non-existent key`() {
        val map = mapOf<String, Any?>()
        val provider = MapConfigFieldProvider(map)
        assertNull(provider.getString("key2"))
    }

    @Test
    fun `test getBoolean returns true for true String value`() {
        val map = mapOf("key" to true)
        val provider = MapConfigFieldProvider(map)
        assertTrue(provider.getBoolean("key")!!)
    }

    @Test
    fun `test getBoolean returns false for false String value`() {
        val map = mapOf("key" to false)
        val provider = MapConfigFieldProvider(map)
        assertFalse(provider.getBoolean("key")!!)
    }

    @Test
    fun `test getBoolean returns null for non-boolean String value`() {
        val map = mapOf("key" to "notABoolean")
        val provider = MapConfigFieldProvider(map)
        assertNull(provider.getBoolean("key"))
    }

    @Test
    fun `test getInt returns correct Int value`() {
        val map = mapOf("key" to "123")
        val provider = MapConfigFieldProvider(map)
        assertEquals(123, provider.getInt("key"))
    }

    @Test
    fun `test getInt returns null for non-integer String value`() {
        val map = mapOf("key" to "notAnInt")
        val provider = MapConfigFieldProvider(map)
        assertThrows(NumberFormatException::class.java) {
            provider.getInt("key")
        }
    }
}
