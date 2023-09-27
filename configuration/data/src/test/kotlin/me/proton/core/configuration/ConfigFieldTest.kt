/*
 * Copyright (c) 2022 Proton Technologies AG
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ConfigFieldTest {

    @Test
    fun `returns correct String value when present in map`() {
        val key = "testKey"
        val expectedValue = "testValue"
        val configMap: Map<String, Any?> = mapOf(key to expectedValue)

        val actualValue: String = configMap.configField(key)

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `returns correct Boolean value when present in map`() {
        val key = "testKey"
        val expectedValue = true
        val configMap: Map<String, Any?> = mapOf(key to expectedValue)

        val actualValue: Boolean = configMap.configField(key)

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `returns null when value in map is null`() {
        val key = "testKey"
        val configMap: Map<String, Any?> = mapOf(key to null)

        val actualValue: String? = configMap.configField(key)

        assertNull(actualValue)
    }

    @Test
    fun `throws exception when value in map is not of expected type`() {
        val key = "testKey"
        val intValue = 123
        val configMap: Map<String, Any?> = mapOf(key to intValue)

        assertThrows(IllegalArgumentException::class.java) {
            configMap.configField(key)
        }
    }

    @Test
    fun `throws exception when key is not present in map`() {
        val key = "missingKey"
        val configMap: Map<String, Any?> = emptyMap()

        assertThrows(IllegalArgumentException::class.java) {
            configMap.configField(key)
        }
    }
}
