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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MutableSharedFlowSavedStateTest {
    @Test
    fun mutableSharedFlowWithValue() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle()
        val flowSavedState = handle.flowState(MutableSharedFlow<String>(), this)
        val flow by flowSavedState
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
            }
        }
        flow.emit("one")
        deferred.await()
        assertEquals("one", handle["property_flow"])

        flowSavedState.stop()
    }

    @Test
    fun mutableSharedFlowWithNull() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle()
        val flowSavedState = handle.flowState(MutableSharedFlow<String?>(), this)
        val flow by flowSavedState
        val deferred = async {
            flow.test {
                assertEquals(null, awaitItem())
            }
        }
        flow.emit(null)
        deferred.await()
        assertNull(handle["property_flow"])

        flowSavedState.stop()
    }

    @Test
    fun mutableSharedFlowWithMultipleValues() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle()
        val flowSavedState = handle.flowState(MutableSharedFlow<String>(), this)
        val flow by flowSavedState
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

        flowSavedState.stop()
    }

    @Test
    fun restoredMutableSharedFlow() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle(mapOf("property_flow" to "one"))
        var restored: String? = null
        val flowSavedState = handle.flowState(MutableSharedFlow<String>(), this) {
            restored = it
        }
        val flow by flowSavedState
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("two")

        deferred.await()
        assertEquals("one", restored)

        flowSavedState.stop()
    }

    @Test
    fun restoredMutableSharedFlowWithReplay() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle(mapOf("property_flow" to "one"))
        var restored: String? = null
        val flowSavedState = handle.flowState(MutableSharedFlow<String>(replay = 1), this) {
            restored = it
        }
        val flow by flowSavedState
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
        flowSavedState.stop()
    }

    @Test
    fun restoredMutableSharedFlowWithNull() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle(mapOf("property_flow" to null))
        var restored: String? = ""
        val flowSavedState = handle.flowState(MutableSharedFlow<String?>(replay = 1), this) { restored = it }
        val flow by flowSavedState
        val deferred = async {
            flow.test {
                assertEquals(null, awaitItem())
                assertEquals("one", awaitItem())
            }
        }
        flow.emit("one")
        deferred.await()
        assertEquals(null, restored)

        flowSavedState.stop()
    }

    @Test
    fun restoredValueWithCustomKey() = runTest(UnconfinedTestDispatcher()) {
        val handle = SavedStateHandle(mapOf("custom_key" to "one"))
        val flowSavedState = handle.flowState(MutableSharedFlow<String>(replay = 1), this, "custom_key")
        val flow by flowSavedState
        val deferred = async {
            flow.test {
                assertEquals("one", awaitItem())
                assertEquals("two", awaitItem())
            }
        }

        flow.emit("two")
        deferred.await()

        flowSavedState.stop()
    }
}
