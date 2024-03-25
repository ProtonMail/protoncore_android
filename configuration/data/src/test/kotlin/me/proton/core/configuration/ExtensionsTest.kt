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

import me.proton.core.configuration.extension.configContractFields
import me.proton.core.configuration.extension.primitiveFieldMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionFunctionTests {

    class TestClass {
        private val stringField: String = "TestString"
        private val booleanField: Boolean = true
        private val intField: Int = 42
    }

    @Test
    fun `configContractFields includes all declared fields`() {
        val testObject = TestClass()
        val fields = testObject.configContractFields

        assertEquals(3, fields.size)
        assertTrue(fields.containsKey("stringField"))
        assertTrue(fields.containsKey("booleanField"))
        assertTrue(fields.containsKey("intField"))
    }

    @Test
    fun `primitiveFieldMap includes only primitive fields`() {
        val testObject = TestClass()
        val primitiveFields = testObject.primitiveFieldMap

        assertEquals(2, primitiveFields.size)
        assertEquals("TestString", primitiveFields["stringField"])
        assertEquals(true, primitiveFields["booleanField"])
        assertFalse(primitiveFields.containsKey("intField"))
    }

    @Test
    fun `configContractFields makes fields accessible`() {
        val testObject = TestClass()
        val fields = testObject.configContractFields

        assertTrue(fields.all { it.value.isAccessible })
    }
}
