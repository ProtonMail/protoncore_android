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

import me.proton.core.configuration.extension.primitiveFieldMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionFunctionTests {
    class MockClass {
        val stringField = "Test String"
        val booleanField = true
        val intField = 123
        val listField = listOf("Not", "Primitive")
        val doubleField = 123.45
    }

    @Test
    fun `primitiveFieldMap includes only primitive fields`() {
        val mockInstance = MockClass()
        val primitiveFieldMap = mockInstance.primitiveFieldMap

        assertEquals(3, primitiveFieldMap.size)
        assertTrue(primitiveFieldMap.containsKey("stringField"))
        assertTrue(primitiveFieldMap.containsKey("booleanField"))
        assertTrue(primitiveFieldMap.containsKey("intField"))

        assertEquals("Test String", primitiveFieldMap["stringField"])
        assertEquals(true, primitiveFieldMap["booleanField"])
        assertEquals(123, primitiveFieldMap["intField"])

        assertTrue(!primitiveFieldMap.containsKey("listField"))
        assertTrue(!primitiveFieldMap.containsKey("doubleField"))
    }
}
