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
import app.cash.turbine.test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

class MutableSharedFlowSavedStateTest {
    @Test
    fun mutableSharedFlowWithValue() = runBlockingTest {
        val handle = SavedStateHandle()
        val scope = TestCoroutineScope()
        val flow by handle.flowState(MutableSharedFlow<String>(), scope)
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
            }
        }
        flow.emit("one")
        deferred.await()
        assertEquals("one", handle["property_flow"])
    }

    @Test
    fun mutableSharedFlowWithNull() = runBlockingTest {
        val handle = SavedStateHandle()
        val scope = TestCoroutineScope()
        val flow by handle.flowState(MutableSharedFlow<String?>(), scope)
        val deferred = async {
            flow.test {
                assertEquals(null, awaitItem())
            }
        }
        flow.emit(null)
        deferred.await()
        assertNull(handle["property_flow"])
    }

    @Test
    fun mutableSharedFlowWithMultipleValues() = runBlockingTest {
        val handle = SavedStateHandle()
        val scope = TestCoroutineScope()
        val flow by handle.flowState(MutableSharedFlow<String>(), scope)
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("one")
        assertEquals("one", handle["property_flow"])

        flow.emit("two")
        deferred.await()
        assertEquals("two", handle["property_flow"])
    }

    @Test
    fun restoredMutableSharedFlow() = runBlockingTest {
        val handle = SavedStateHandle(mapOf("property_flow" to "one"))
        val scope = TestCoroutineScope()
        var restored: String? = null
        val flow by handle.flowState(MutableSharedFlow<String>(), scope) {
            restored = it
        }
        val deferred = async {
            flow.test {
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("two")

        deferred.await()
        assertEquals("one", restored)
    }

    @Test
    fun restoredMutableSharedFlowWithReplay() = runBlockingTest {
        val handle = SavedStateHandle(mapOf("property_flow" to "one"))
        val scope = TestCoroutineScope()
        var restored: String? = null
        val flow by handle.flowState(MutableSharedFlow<String>(replay = 1), scope) {
            restored = it
        }
        val collectorA = async {
            flow.test {
                assertEquals("one", awaitItem())
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("two")

        val collectorB = async {
            flow.test {
                assertEquals("two", awaitItem())
            }
        }

        awaitAll(collectorA, collectorB)
        assertEquals("one", restored)
    }

    @Test
    fun restoredMutableSharedFlowWithNull() = runBlockingTest {
        val handle = SavedStateHandle(mapOf("property_flow" to null))
        val scope = TestCoroutineScope()
        var restored: String? = ""
        val flow by handle.flowState(MutableSharedFlow<String?>(replay = 1), scope) { restored = it }
        val deferred = async {
            flow.test {
                assertEquals(null, awaitItem())
                assertEquals("one", awaitItem())
            }
        }
        flow.emit("one")
        deferred.await()
        assertEquals(null, restored)
    }

    @Test
    fun restoredValueWithCustomKey() = runBlockingTest {
        val handle = SavedStateHandle(mapOf("custom_key" to "one"))
        val scope = TestCoroutineScope()
        val flow by handle.flowState(MutableSharedFlow<String>(replay = 1), scope, "custom_key")
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("two")
        deferred.await()
    }
}
