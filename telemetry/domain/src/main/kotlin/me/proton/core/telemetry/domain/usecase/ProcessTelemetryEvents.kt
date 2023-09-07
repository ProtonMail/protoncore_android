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

package me.proton.core.telemetry.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import javax.inject.Inject

/** Processes telemetry events in batches and sends them to the server. */
public class ProcessTelemetryEvents @Inject constructor(
    private val isTelemetryEnabled: IsTelemetryEnabled,
    private val repository: TelemetryRepository
) {
    public suspend operator fun invoke(userId: UserId?) {
        when (isTelemetryEnabled(userId)) {
            true -> processSingleBatch(userId)
            else -> repository.deleteAllEvents(userId)
        }
    }

    private tailrec suspend fun processSingleBatch(userId: UserId?) {
        val events = repository.getEvents(userId, BATCH_SIZE)
        if (events.isEmpty()) return

        repository.sendEvents(userId, events)
        repository.deleteEvents(userId, events)

        processSingleBatch(userId)
    }

    private companion object {
        private const val BATCH_SIZE = 100
    }
}
