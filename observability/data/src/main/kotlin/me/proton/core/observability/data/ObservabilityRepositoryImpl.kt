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

import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.observability.data.entity.ObservabilityEventEntity
import me.proton.core.observability.data.entity.toObservabilityEventEntity
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.util.kotlin.serialize
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
        observabilityDao.deleteAll(
            events.mapNotNull { it.id }
        )
    }

    override suspend fun deleteEvent(event: ObservabilityEvent) {
        observabilityDao.delete(event.toObservabilityEventEntity())
    }

    override suspend fun getEvents(limit: Int): List<ObservabilityEvent> =
        observabilityDao.getAll(limit).map {
            it.toObservabilityEvent()
        }

    override suspend fun getEventCount(): Long = observabilityDao.getCount()
}