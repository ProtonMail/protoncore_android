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
import me.proton.core.test.android.mocks.newMockSharedPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for Preferences' DSL
 * @author Davide Farella
 */
internal class PreferencesUtilsTest {

    private val p = newMockSharedPreferences

    // region get - set
    // region get
    @Test
    fun `get non null Int by type inference`() {
        val number = p.get("key", 4)
        assertEquals(4, number)
    }

    @Test
    fun `get nullable Int by type inference`() {
        val number: Int? = p["key"]
        assertEquals(null, number)
    }
    // endregion

    // region set
    @Test
    fun `set by type inference`() {
        p.edit { put("key", 5) }
        @Suppress("RemoveExplicitTypeArguments") // Bug: It is needed
        assertEquals(5, p.get<Int?>("key"))
    }
    // endregion

    // region set + get by type
    @Test
    fun `set and get for Boolean`() {
        p["key"] = true
        assertEquals(true, p.get("key", false))
    }

    @Test
    fun `set and get for Float`() {
        p["key"] = 5f
        assertEquals(5f, p.get("key", 0f))
    }

    @Test
    fun `set and get for Int`() {
        p["key"] = 6
        assertEquals(6, p.get("key", 0))
    }

    @Test
    fun `set and get for Long`() {
        p["key"] = 7L
        assertEquals(7L, p.get("key", 0L))
    }

    @Test
    fun `set and get for String`() {
        p["key"] = "hi"
        assertEquals("hi", p.get("key", "bye"))
    }

    @Test
    fun `set and get for StringSet`() {
        p["key"] = setOf("hello", "world")
        assertEquals(setOf("hello", "world"), p.get("key", emptySet()))
    }
    // endregion

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
    fun `clearAll remove all preferences`() {
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
