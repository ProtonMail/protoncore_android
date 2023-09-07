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
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryLocalDataSource
import me.proton.core.telemetry.domain.repository.TelemetryRemoteDataSource
import javax.inject.Inject

public class TelemetryRepositoryImpl @Inject constructor(
    private val localDataSource: TelemetryLocalDataSource,
    private val remoteDataSource: TelemetryRemoteDataSource
) : TelemetryRepository {

    override suspend fun addEvent(userId: UserId?, event: TelemetryEvent) {
        localDataSource.addEvent(userId, event)
    }

    override suspend fun deleteAllEvents(userId: UserId?) {
        localDataSource.deleteAllEvents(userId)
    }

    override suspend fun deleteEvents(userId: UserId?, events: List<TelemetryEvent>) {
        localDataSource.deleteEvents(userId, events)
    }

    override suspend fun getEvents(userId: UserId?, limit: Int): List<TelemetryEvent> =
        localDataSource.getEvents(userId, limit)

    override suspend fun sendEvents(userId: UserId?, events: List<TelemetryEvent>) {
        remoteDataSource.sendEvents(userId, events)
    }
}
