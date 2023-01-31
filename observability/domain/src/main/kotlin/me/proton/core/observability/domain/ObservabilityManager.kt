/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.observability.domain

import kotlinx.coroutines.launch
import me.proton.core.observability.domain.entity.ObservabilityData
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.serialize
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

public class ObservabilityManager @Inject internal constructor(
    private val isObservabilityEnabled: IsObservabilityEnabled,
    private val repository: ObservabilityRepository,
    private val scopeProvider: CoroutineScopeProvider,
    private val workerManager: ObservabilityWorkerManager,
) {
    /** Enqueues an event with a given [data] and [timestamp] to be sent at some point in the future.
     * If observability is disabled, the event won't be sent.
     */
    public fun enqueue(data: ObservabilityData, timestamp: Instant = Instant.now()) {
        enqueue(
            ObservabilityEvent(
                timestamp = timestamp,
                data = data
            )
        )
    }

    /** Enqueues an [event] to be sent at some point in the future.
     * If observability is disabled, the event won't be sent
     */
    public fun enqueue(event: ObservabilityEvent) {
        scopeProvider.GlobalIOSupervisedScope.launch {
            enqueueEvent(event)
        }
    }

    private suspend fun enqueueEvent(event: ObservabilityEvent) {
        if (isObservabilityEnabled()) {
            repository.addEvent(event)
            workerManager.schedule(getSendDelay())
        } else {
            workerManager.cancel()
            repository.deleteAllEvents()
        }
    }

    private suspend fun getSendDelay(): Duration = when {
        repository.getEventCount() >= MAX_EVENT_COUNT -> ZERO
        workerManager.getDurationSinceLastShipment()?.let { it > MAX_DELAY_MS.milliseconds } ?: false -> ZERO
        else -> MAX_DELAY_MS.milliseconds
    }

    private companion object {
        private const val MAX_EVENT_COUNT = 100
        private const val MAX_DELAY_MS = 2 * 60 * 1000
    }
}
