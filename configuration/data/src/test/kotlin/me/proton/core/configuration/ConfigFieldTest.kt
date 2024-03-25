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
import org.junit.Test

class ConfigFieldTest {

    @Test
    fun `returns correct String value when present in map`() {
        val key = "testKey"
        val expectedValue = "testValue"
        val configMap: Map<String, Any?> = mapOf(key to expectedValue)

        val actualValue: String = configMap[key] as String

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `returns correct Boolean value when present in map`() {
        val key = "testKey"
        val expectedValue = true
        val configMap: Map<String, Any?> = mapOf(key to expectedValue)

        val actualValue: Boolean = configMap[key] as Boolean

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `returns null when value in map is null`() {
        val key = "testKey"
        val configMap: Map<String, Any?> = mapOf(key to null)

        val actualValue: String? = configMap[key] as String?

        assertNull(actualValue)
    }
}
