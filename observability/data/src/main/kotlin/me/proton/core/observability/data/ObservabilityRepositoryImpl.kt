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

package me.proton.core.observability.data

import kotlinx.serialization.SerializationException
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.toObservabilityEventEntity
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.entity.ObservabilityEvent
import javax.inject.Inject

public class ObservabilityRepositoryImpl @Inject constructor(
    db: ObservabilityDatabase
) : ObservabilityRepository {

    private val observabilityDao = db.observabilityDao()

    override suspend fun addEvent(event: ObservabilityEvent) {
        observabilityDao.insertOrUpdate(event.toObservabilityEventEntity())
    }

    override suspend fun deleteAllEvents() {
        observabilityDao.deleteAll()
    }

    override suspend fun deleteEvents(events: List<ObservabilityEvent>) {
        observabilityDao.delete(*events.map { it.toObservabilityEventEntity() }.toTypedArray())
    }

    override suspend fun deleteEvent(event: ObservabilityEvent) {
        observabilityDao.delete(event.toObservabilityEventEntity())
    }

    override suspend fun getEventsAndSanitizeDb(limit: Int?): List<ObservabilityEvent> {
        val events = if (limit == null)
            observabilityDao.getAll()
        else
            observabilityDao.getAll(limit)
        val result = events.mapNotNull {
            it.toObservabilityEvent()
        }
        return result
    }

    override suspend fun getEventCount(): Long = observabilityDao.getCount()
}