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

package me.proton.core.telemetry.data.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.data.db.TelemetryDatabase
import me.proton.core.telemetry.data.entity.toTelemetryEventEntity
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryLocalDataSource
import javax.inject.Inject

public class TelemetryLocalDataSourceImpl @Inject constructor(
    db: TelemetryDatabase
) : TelemetryLocalDataSource {

    private val telemetryDao = db.telemetryDao()

    override suspend fun addEvent(userId: UserId?, event: TelemetryEvent) {
        telemetryDao.insertOrUpdate(event.toTelemetryEventEntity(userId))
    }

    override suspend fun deleteAllEvents(userId: UserId?) {
        if (userId == null) {
            telemetryDao.deleteAllUnAuth()
        } else {
            telemetryDao.deleteAll(userId)
        }
    }

    override suspend fun deleteEvents(userId: UserId?, events: List<TelemetryEvent>) {
        telemetryDao.delete(*events.map { it.toTelemetryEventEntity(userId) }.toTypedArray())
    }

    override suspend fun getEvents(userId: UserId?, limit: Int): List<TelemetryEvent> {
        val events = if (userId == null) {
            telemetryDao.getAllUnAuth(limit)
        } else {
            telemetryDao.getAll(userId, limit)
        }
        return events.map { telemetryEventEntity ->
            telemetryEventEntity.toTelemetryEvent()
        }
    }
}
