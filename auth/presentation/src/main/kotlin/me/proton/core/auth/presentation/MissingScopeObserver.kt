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

package me.proton.core.auth.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeState
import javax.inject.Singleton

@Singleton
class MissingScopeObserver(
    private val lifecycle: Lifecycle,
    private val minActiveState: Lifecycle.State = Lifecycle.State.CREATED,
    private val missingScopeListener: MissingScopeListener,
    private val scope: CoroutineScope = lifecycle.coroutineScope
) {
    private val observerJobs = mutableListOf<Job>()

    internal fun addMissingScopeStateListener(
        block: suspend (MissingScopeState) -> Unit
    ) {
        observerJobs += missingScopeListener.stateFlow
            .flowWithLifecycle(lifecycle, minActiveState)
            .onEach { block(it) }
            .launchIn(scope)
    }

    fun cancelAllObservers() {
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }
}

fun MissingScopeObserver.onMissingScope(
    block: suspend (MissingScopeState) -> Unit
): MissingScopeObserver {
    addMissingScopeStateListener(
        block = block
    )
    return this
}
