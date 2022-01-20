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

package me.proton.core.humanverification.presentation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.onHumanVerificationState
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState

class HumanVerificationManagerObserver(
    private val lifecycle: Lifecycle,
    private val minActiveState: Lifecycle.State = Lifecycle.State.CREATED,
    internal val humanVerificationManager: HumanVerificationManager,
    internal val scope: CoroutineScope = lifecycle.coroutineScope,
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val observerJobs = mutableListOf<Job>()

    internal fun addHumanVerificationStateListener(
        state: HumanVerificationState,
        initialState: Boolean,
        block: suspend (HumanVerificationDetails) -> Unit
    ) {
        observerJobs += humanVerificationManager.onHumanVerificationState(state, initialState = initialState)
            .flowWithLifecycle(lifecycle, minActiveState)
            .onEach { block(it) }
            .launchIn(scope)
    }

    fun cancelAllObservers() {
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }
}

fun HumanVerificationManager.observe(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.CREATED
) = HumanVerificationManagerObserver(lifecycle, minActiveState, this)

fun HumanVerificationManagerObserver.onHumanVerificationNeeded(
    initialState: Boolean = false,
    block: suspend (HumanVerificationDetails) -> Unit
): HumanVerificationManagerObserver {
    addHumanVerificationStateListener(
        state = HumanVerificationState.HumanVerificationNeeded,
        initialState = initialState,
        block = block
    )
    return this
}

fun HumanVerificationManagerObserver.onHumanVerificationFailed(
    initialState: Boolean = true,
    block: suspend (HumanVerificationDetails) -> Unit
): HumanVerificationManagerObserver {
    addHumanVerificationStateListener(
        state = HumanVerificationState.HumanVerificationFailed,
        initialState = initialState,
        block = block
    )
    return this
}

fun HumanVerificationManagerObserver.onHumanVerificationSucceeded(
    initialState: Boolean = true,
    block: suspend (HumanVerificationDetails) -> Unit
): HumanVerificationManagerObserver {
    addHumanVerificationStateListener(
        state = HumanVerificationState.HumanVerificationSuccess,
        initialState = initialState,
        block = block
    )
    return this
}
