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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Provides a read/write delegate, that makes sure to save and restore current value using [SavedStateHandle].
 * This is similar to [state], but the initial value is not required upfront (similar to a `lateinit var`).
 * @param savedStateHandleKey Optional custom key to use when reading from [SavedStateHandle].
 * @param T Any type that can be accepted by Bundle.
 * @sample me.proton.core.presentation.savedstate.SavedStateTest.lateState
 */
fun <T : Any> SavedStateHandle.lateState(savedStateHandleKey: String? = null): LateSavedState<T> {
    return LateSavedState(this, savedStateHandleKey)
}

/** Provides a read/write delegate, that makes sure to save and restore current value using [SavedStateHandle].
 * @param initialValue The value that will be used if no previous state was restored.
 * @param savedStateHandleKey Optional custom key to use when reading from [SavedStateHandle].
 * @param T Any type that can be accepted by Bundle.
 * @sample me.proton.core.presentation.savedstate.SavedStateTest.defaultState
 */
fun <T> SavedStateHandle.state(initialValue: T, savedStateHandleKey: String? = null): SavedState<T> {
    return SavedState(initialValue, this, savedStateHandleKey)
}

class SavedState<T>(initialValue: T, savedStateHandle: SavedStateHandle, savedStateHandleKey: String?) :
    BaseSavedState<T>(savedStateHandle, savedStateHandleKey) {
    override var currentValue: T = initialValue
}

class LateSavedState<T : Any>(savedStateHandle: SavedStateHandle, savedStateHandleKey: String?) :
    BaseSavedState<T>(savedStateHandle, savedStateHandleKey) {
    override lateinit var currentValue: T

    val isInitialized: Boolean
        get() = this::currentValue.isInitialized
}

/**
 * @param T Any type that can be accepted by Bundle.
 * @param savedStateHandleKey The key to use when saving/restoring the state; if null then the key will be derived from [KProperty]
 */
abstract class BaseSavedState<T>(
    private val savedStateHandle: SavedStateHandle,
    private val savedStateHandleKey: String?
) : ReadWriteProperty<Any?, T> {
    abstract var currentValue: T
    private var didLoadFromSavedState: Boolean = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val key = getSavedStateHandleKey(savedStateHandleKey, property)
        if (!didLoadFromSavedState) {
            didLoadFromSavedState = true
            if (savedStateHandle.contains(key)) {
                @Suppress("UNCHECKED_CAST")
                currentValue = savedStateHandle.get<T>(key) as T
            }
        }
        return currentValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = getSavedStateHandleKey(savedStateHandleKey, property)
        currentValue = value
        savedStateHandle.set(key, value)
    }
}

internal fun getSavedStateHandleKey(savedStateHandleKey: String?, property: KProperty<*>): String =
    savedStateHandleKey ?: "property_${property.name}"
