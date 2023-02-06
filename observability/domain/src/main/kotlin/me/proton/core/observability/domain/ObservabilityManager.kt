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
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
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

    private suspend fun getSendDelay(): Duration {
        val eventCount = repository.getEventCount()
        return when {
            eventCount <= 1L -> MAX_DELAY_MS.milliseconds
            eventCount >= MAX_EVENT_COUNT -> ZERO
            workerManager.getDurationSinceLastShipment()?.let { it >= MAX_DELAY_MS.milliseconds } ?: false -> ZERO
            else -> MAX_DELAY_MS.milliseconds
        }
    }

    internal companion object {
        internal const val MAX_EVENT_COUNT = 50L
        internal const val MAX_DELAY_MS = 30 * 1000L
    }
}

/** Enqueues an observability data event from the [result].
 * The event is recorded if [metricData] is not null.
 * Useful for metrics that can create [ObservabilityData] from [HttpApiStatus].
 **/
public fun <R> ObservabilityManager.enqueueFromResult(
    metricData: ((HttpApiStatus) -> ObservabilityData)?,
    result: Result<R>
): Result<R> {
    metricData ?: return result
    return result.onSuccess {
        enqueue(metricData(HttpApiStatus.http2xx))
    }.onFailure {
        enqueue(metricData(it.toHttpApiStatus()))
    }
}

/** Enqueues an observability data event from the [Result] of executing a [block].
 * The event is recorded if [metricData] is not null.
 * Useful for metrics that can create [ObservabilityData] from [HttpApiStatus].
 **/
public suspend fun <T, R> T.runWithObservability(
    observabilityManager: ObservabilityManager,
    metricData: ((HttpApiStatus) -> ObservabilityData)?,
    block: suspend T.() -> R
): R = runCatching {
    block()
}.also {
    observabilityManager.enqueueFromResult(metricData, it)
}.getOrThrow()
