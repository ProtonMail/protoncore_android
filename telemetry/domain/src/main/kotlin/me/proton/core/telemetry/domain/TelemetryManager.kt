/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

public class TelemetryManager @Inject internal constructor(
    private val isTelemetryEnabled: IsTelemetryEnabled,
    private val repository: TelemetryRepository,
    private val scopeProvider: CoroutineScopeProvider,
    private val workerManager: TelemetryWorkerManager
) {

    /**
     * Enqueues an [event] to be sent at some point in the future.
     *
     * If telemetry is disabled, the event won't be sent.
     */
    public fun enqueue(
        userId: UserId?,
        event: TelemetryEvent,
        priority: TelemetryPriority = TelemetryPriority.Default
    ) {
        scopeProvider.GlobalIOSupervisedScope.launch {
            enqueueEvent(userId, event, priority)
        }
    }

    @VisibleForTesting
    internal suspend fun enqueueEvent(
        userId: UserId?,
        event: TelemetryEvent,
        priority: TelemetryPriority = TelemetryPriority.Default
    ) {
        CoreLogger.d(LogTag.ENQUEUE, "$event")
        runCatching {
            if (isTelemetryEnabled(userId)) {
                repository.addEvent(userId, event)
                when (priority) {
                    TelemetryPriority.Immediate -> workerManager.enqueueAndReplace(userId, MIN_DELAY)
                    TelemetryPriority.Default -> workerManager.enqueueOrKeep(userId, MAX_DELAY)
                }
            } else {
                workerManager.cancel(userId)
                repository.deleteAllEvents(userId)
            }
        }.onFailure {
            CoreLogger.e(LogTag.ENQUEUE, it)
        }
    }

    internal companion object {
        internal val MIN_DELAY = 1.seconds
        internal val MAX_DELAY = 1.hours
    }
}
