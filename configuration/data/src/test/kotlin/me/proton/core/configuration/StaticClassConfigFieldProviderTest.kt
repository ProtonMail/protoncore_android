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

import me.proton.core.configuration.provider.StaticClassConfigFieldProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test


class StaticClassConfigFieldProviderTest {
    @Test
    fun `getString returns correct String value`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertEquals("stringValue", provider.getString("stringKey"))
    }

    @Test
    fun `getBoolean returns correct Boolean value`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertTrue(provider.getBoolean("booleanKey")!!)
    }

    @Test
    fun `getInt returns correct Int value`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertEquals(123, provider.getInt("intKey"))
    }

    @Test
    fun `getString returns null for non-existent key`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertNull(provider.getString("nonExistentKey"))
    }

    @Test
    fun `getBoolean returns null for non-existent key`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertNull(provider.getBoolean("nonExistentKey"))
    }

    @Test
    fun `getInt returns null for non-existent key`() {
        val provider = StaticClassConfigFieldProvider(TestClass::class.java.name)
        assertNull(provider.getInt("nonExistentKey"))
    }

    @Test
    fun `throws exception for non-existent class`() {
        val nonExistentClassName = "com.example.NonExistentClass"
        assertThrows(IllegalStateException::class.java) {
            StaticClassConfigFieldProvider(nonExistentClassName)
        }
    }
}

class TestClass {
    val stringKey: String = "stringValue"
    val booleanKey: Boolean = true
    val intKey: Int = 123
}
