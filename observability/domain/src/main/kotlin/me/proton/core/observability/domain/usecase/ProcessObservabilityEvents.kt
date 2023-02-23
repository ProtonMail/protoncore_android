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

package me.proton.core.observability.domain.usecase

import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.ObservabilityTimeTracker
import javax.inject.Inject

/** Processes observability events in batches and sends them to the server. */
public class ProcessObservabilityEvents @Inject constructor(
    private val isObservabilityEnabled: IsObservabilityEnabled,
    private val repository: ObservabilityRepository,
    private val timeTracker: ObservabilityTimeTracker,
    private val sendObservabilityEvents: SendObservabilityEvents
) {
    public suspend operator fun invoke() {
        when (isObservabilityEnabled()) {
            true -> processSingleBatch()
            else -> repository.deleteAllEvents()
        }
        timeTracker.clear()
    }

    private tailrec suspend fun processSingleBatch() {
        val events = repository.getEvents(BATCH_SIZE)
        if (events.isEmpty()) return

        sendObservabilityEvents(events)
        repository.deleteEvents(events)

        processSingleBatch()
    }

    private companion object {
        private const val BATCH_SIZE = 100
    }
}
