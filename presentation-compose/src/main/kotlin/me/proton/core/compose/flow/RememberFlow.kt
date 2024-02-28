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

package me.proton.core.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Merges functionality from Compose [androidx.compose.runtime.remember] and flow's [flowWithLifecycle].
 *
 * Can be used in a Composable function to remember the flow (view state) from a ViewModel:
 * ```
 * val viewState by rememberFlowWithLifecycle(viewModel.viewState)
 *     .collectAsState(initial = ViewState.initialValue)
 * ```
 *
 * @see [rememberAsState]
 */
@Deprecated(
    message = "Use Flow.collectAsStateWithLifecycle(initialValue) or StateFlow.collectAsStateWithLifecycle()",
    replaceWith = ReplaceWith(
        "flow.collectAsStateWithLifecycle()",
        "androidx.lifecycle.compose.collectAsStateWithLifecycle"
    )
)
@Composable
fun <T> rememberFlowWithLifecycle(
    flow: Flow<T>,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> = remember(flow, lifecycle) {
    flow.flowWithLifecycle(
        lifecycle = lifecycle,
        minActiveState = minActiveState
    )
}

/**
 * Merges [rememberFlowWithLifecycle] and [collectAsState].
 *
 * Can be used in a Composable function to remember the flow (view state) from a ViewModel:
 * ```
 * val viewState by rememberAsState(viewModel.viewState, ViewState.initialValue)
 * ```
 */
@Deprecated(
    message = "Use Flow.collectAsStateWithLifecycle(initialValue) or StateFlow.collectAsStateWithLifecycle()",
    replaceWith = ReplaceWith(
        "flow.collectAsStateWithLifecycle(initial)",
        "androidx.lifecycle.compose.collectAsStateWithLifecycle"
    )
)
@Composable
fun <T> rememberAsState(
    flow: Flow<T>,
    initial: T
): State<T> = flow.collectAsStateWithLifecycle(initial)

@Deprecated(
    message = "Use StateFlow.collectAsStateWithLifecycle()",
    replaceWith = ReplaceWith(
        "flow.collectAsStateWithLifecycle()",
        "androidx.lifecycle.compose.collectAsStateWithLifecycle"
    )
)
@Composable
fun <T> rememberAsState(
    flow: StateFlow<T>,
    initial: T
): State<T> = flow.collectAsStateWithLifecycle()
