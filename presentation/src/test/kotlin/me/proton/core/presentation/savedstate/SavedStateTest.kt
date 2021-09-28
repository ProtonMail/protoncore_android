/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.presentation.savedstate

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFails

class SavedStateTest {
    @Test
    fun defaultState() {
        val handle = SavedStateHandle()
        var greeting by handle.state("hello")
        assertEquals("hello", greeting)
        greeting = "bye"
        assertEquals("bye", greeting)
        assertEquals("bye", handle.get("property_greeting"))
    }

    @Test
    fun defaultNullState() {
        val handle = SavedStateHandle()
        var greeting by handle.state<String?>(null)
        assertEquals(null, greeting)
        greeting = "bye"
        assertEquals("bye", greeting)
        assertEquals("bye", handle.get("property_greeting"))
    }

    @Test
    fun lateState() {
        val handle = SavedStateHandle()
        val state = handle.lateState<String>()
        var greeting by state

        assertFalse(state.isInitialized)
        assertFails { state.currentValue }
        val throwable = assertFails { greeting.hashCode() }
        assertEquals(UninitializedPropertyAccessException::class.java, throwable::class.java)

        greeting = "hi"
        assertTrue(state.isInitialized)
        assertEquals("hi", state.currentValue)
        assertEquals("hi", greeting)
    }

    @Test
    fun storedState() {
        val handle = SavedStateHandle(mapOf("property_greeting" to "hey"))
        val greeting by handle.state("welcome")
        assertEquals("hey", greeting)
    }

    @Test
    fun storedNullState() {
        val handle = SavedStateHandle(mapOf("property_greeting" to null))
        val greeting by handle.state("welcome")
        assertEquals(null, greeting)
    }

    @Test
    fun storedLateState() {
        val handle = SavedStateHandle(mapOf("property_greeting" to "hi"))
        val greeting by handle.lateState<String>()
        assertEquals("hi", greeting)
    }

    @Test
    fun stateWithCustomKey() {
        val handle = SavedStateHandle(mapOf("my_state" to "hi"))
        var greeting by handle.state("hey", "my_state")
        assertEquals("hi", greeting)

        greeting = "bye"
        assertEquals("bye", greeting)
        assertEquals("bye", handle["my_state"])
    }

    @Test
    fun lateStateWithCustomKey() {
        val handle = SavedStateHandle()
        var greeting by handle.lateState<String>("my_state")
        greeting = "bye"
        assertEquals("bye", greeting)
        assertEquals("bye", handle["my_state"])
    }
}
