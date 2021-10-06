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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** Provides a read-only delegate, that makes sure to save and restore the most recent value of the [mutableSharedFlow] using [SavedStateHandle].
 * Only the most recent value will be restored (even if your [mutableSharedFlow] uses a replay or an extra buffer).
 * @param mutableSharedFlow The flow for which the most recent value will be saved and restored.
 * @param coroutineScope Scope that will be used to save and restore values from the [mutableSharedFlow].
 * @param savedStateHandleKey Optional custom key to use when reading from [SavedStateHandle].
 * @param onStateRestored Called when a value has been restored from [SavedStateHandle].
 * @param T Any type that can be accepted by Bundle.
 * @sample me.proton.core.presentation.savedstate.MutableSharedFlowSavedStateTest
 */
fun <T> SavedStateHandle.flowState(
    mutableSharedFlow: MutableSharedFlow<T>,
    coroutineScope: CoroutineScope,
    savedStateHandleKey: String? = null,
    onStateRestored: ((T) -> Unit)? = null
): MutableSharedFlowSavedState<T> {
    return MutableSharedFlowSavedState(coroutineScope, mutableSharedFlow, onStateRestored, this, savedStateHandleKey)
}

class MutableSharedFlowSavedState<T>(
    private val coroutineScope: CoroutineScope,
    private val mutableSharedFlow: MutableSharedFlow<T>,
    private val onStateRestored: ((T) -> Unit)?,
    private val savedStateHandle: SavedStateHandle,
    private val savedStateHandleKey: String?
) : ReadOnlyProperty<Any?, MutableSharedFlow<T>> {
    private var isInitialized = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): MutableSharedFlow<T> {
        if (!isInitialized) {
            isInitialized = true
            val key = getSavedStateHandleKey(savedStateHandleKey, property)
            loadSavedState(key)
            observeFlowValues(key)
        }
        return mutableSharedFlow
    }

    private fun loadSavedState(key: String) = coroutineScope.launch {
        if (savedStateHandle.contains(key)) {
            @Suppress("UNCHECKED_CAST")
            val restoredValue = savedStateHandle.get<T>(key) as T
            mutableSharedFlow.emit(restoredValue)
            onStateRestored?.invoke(restoredValue)
        }
    }

    private fun observeFlowValues(key: String) {
        mutableSharedFlow
            .onEach { savedStateHandle.set(key, it) }
            .launchIn(coroutineScope)
    }
}
