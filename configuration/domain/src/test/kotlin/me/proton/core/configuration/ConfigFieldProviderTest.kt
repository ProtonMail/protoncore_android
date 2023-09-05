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

import me.proton.core.configuration.entity.ConfigFieldProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConfigFieldProviderTest {

    private lateinit var configFieldProvider: ConfigFieldProvider

    val mockConfigData: Map<String, Any?> = mapOf(
        "stringField" to "stringValue",
        "booleanField" to true
    )

    @Before
    fun setUp() {
        configFieldProvider = object : ConfigFieldProvider {
            override val configData: Map<String, Any?> = mockConfigData
        }
    }

    @Test
    fun `test stringProvider with existing field`() {
        assertEquals("stringValue", configFieldProvider.stringProvider("stringField"))
    }

    @Test
    fun `test stringProvider with non-existing field`() {
        assertNull(configFieldProvider.stringProvider("nonExistentField"))
    }

    @Test
    fun `test booleanProvider with existing field`() {
        assertTrue(configFieldProvider.booleanProvider("booleanField")!!)
    }

    @Test
    fun `test booleanProvider with non-existing field`() {
        assertNull(configFieldProvider.booleanProvider("nonExistentField"))
    }
}
