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

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/** A "screen view" event takes place during the state defined by this value. */
private val SCREEN_VIEW_COLLECTION_STATE = Lifecycle.State.CREATED

/** Executes [block] whenever we are in a state that corresponds to a "screen view".
 * The best practice is to call this function when the lifecycle is initialized,
 * (e.g. in [ComponentActivity.onCreate]),
 * to avoid multiple repeating coroutines doing the same thing.
 * @see [Lifecycle.repeatOnLifecycle]
 */
fun LifecycleOwner.launchOnScreenView(block: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(SCREEN_VIEW_COLLECTION_STATE) {
            block()
        }
    }
}

/** Executes [block] whenever we are in a state that corresponds to a "screen view".
 * This function should be called in [Fragment.onCreateView] or [Fragment.onViewCreated].
 * @see [LifecycleOwner.launchOnScreenView]
 */
fun Fragment.launchOnScreenView(block: suspend () -> Unit) {
    viewLifecycleOwner.launchOnScreenView(block)
}
