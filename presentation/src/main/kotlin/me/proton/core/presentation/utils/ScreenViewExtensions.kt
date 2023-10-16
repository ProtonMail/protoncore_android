/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.presentation.utils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistry
import kotlinx.coroutines.launch

private const val SAVED_STATE_PROVIDER_KEY = "core.telemetry.screen_metrics"

/** Executes [block] whenever we are in a state that corresponds to a "screen view".
 * The best practice is to call this function when the lifecycle is initialized,
 * (e.g. in [ComponentActivity.onCreate]).
 */
fun ComponentActivity.launchOnScreenView(block: suspend () -> Unit): () -> Unit =
    launchOnScreenView(savedStateRegistry, block)

/** Executes [block] whenever we are in a state that corresponds to a "screen view".
 * This function should be called in [Fragment.onCreateView] or [Fragment.onViewCreated].
 */
fun Fragment.launchOnScreenView(block: suspend () -> Unit): () -> Unit =
    launchOnScreenView(savedStateRegistry, block)

fun LifecycleOwner.launchOnScreenView(
    savedStateRegistry: SavedStateRegistry,
    block: suspend () -> Unit
): () -> Unit {
    if (savedStateRegistry.getSavedStateProvider(SAVED_STATE_PROVIDER_KEY) == null) {
        savedStateRegistry.registerSavedStateProvider(SAVED_STATE_PROVIDER_KEY) { Bundle.EMPTY }
    }

    val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            if (savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE_PROVIDER_KEY) == null) {
                lifecycleScope.launch { block() }
            }
        }
    }
    lifecycle.addObserver(lifecycleObserver)

    return {
        savedStateRegistry.unregisterSavedStateProvider(SAVED_STATE_PROVIDER_KEY)
        lifecycle.removeObserver(lifecycleObserver)
    }
}
