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
import kotlinx.serialization.SerializationException
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
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
     *
     * Note: events that might produce [SerializationException] will not not be included.
     */
    public fun enqueue(data: ObservabilityData, timestamp: Instant = Instant.now()) {
        runCatching { enqueue(ObservabilityEvent(timestamp = timestamp, data = data))}
            .onFailure { CoreLogger.e(LogTag.ENQUEUE, it) }

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
        CoreLogger.d(LogTag.ENQUEUE, "$event")
        if (isObservabilityEnabled()) {
            repository.addEvent(event)
            workerManager.enqueueOrKeep(MAX_DELAY_MS.milliseconds)
        } else {
            workerManager.cancel()
            repository.deleteAllEvents()
        }
    }

    internal companion object {
        internal const val MAX_EVENT_COUNT = 50L
        internal const val MAX_DELAY_MS = 30 * 1000L
    }
}
