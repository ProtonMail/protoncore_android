/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.util.android.sharedpreferences

import androidx.core.content.edit
import me.proton.core.test.android.mocks.mockSharedPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for Preferences's DSL
 * @author Davide Farella
 */
internal class PreferencesUtilsTest {

    private val p = mockSharedPreferences

    // region get - set
    @Test
    fun `get non null by type inference`() {
        val number = p.get("key", 4)
        assertEquals(4, number)
    }

    @Test
    fun `get nullable by type inference`() {
        val number: Int? = p["key"]
        assertEquals(null, number)
    }

    @Test
    fun `set by type inference`() {
        p.edit { put("key", 5) }
        @Suppress("RemoveExplicitTypeArguments") // Bug: It is needed
        assertEquals(5, p.get<Int?>("key"))
    }

    @Test
    fun `remove by minusAssign`() {
        p["key"] = 5
        assertTrue("key" in p)

        p -= "key"
        assertFalse("key" in p)
    }

    @Test
    fun `minusAssign doesn't throw exception if key is not found`() {
        p -= "hello"
    }
    // endregion

    // region clear
    @Test
    fun `clearAll removed all preferences`() {
        p.edit {
            put("key1", 1)
            put("key2", true)
            put("key3", "hello")
        }
        assertTrue(p.contains("key1"))
        assertTrue(p.contains("key2"))
        assertTrue(p.contains("key3"))

        p.clearAll()
        assertFalse(p.contains("key1"))
        assertFalse(p.contains("key2"))
        assertFalse(p.contains("key3"))
    }

    @Test
    fun `clearAll respect exceptions`() {
        p.edit {
            put("key1", 1)
            put("key2", true)
            put("key3", "hello")
        }
        assertTrue(p.contains("key1"))
        assertTrue(p.contains("key2"))
        assertTrue(p.contains("key3"))

        p.clearAll("key2", "key3")
        assertFalse(p.contains("key1"))
        assertTrue(p.contains("key2"))
        assertTrue(p.contains("key3"))
    }

    @Test
    fun `clearOnly works correctly`() {
        p.edit {
            put("key1", 1)
            put("key2", true)
            put("key3", "hello")
        }
        assertTrue(p.contains("key1"))
        assertTrue(p.contains("key2"))
        assertTrue(p.contains("key3"))

        p.clearOnly("key2", "key3")
        assertTrue(p.contains("key1"))
        assertFalse(p.contains("key2"))
        assertFalse(p.contains("key3"))
    }
    // endregion
}
