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
import me.proton.core.network.data.ApiProvider
import me.proton.core.telemetry.data.api.TelemetryApi
import me.proton.core.telemetry.data.api.request.DataStatsMultipleRequest
import me.proton.core.telemetry.data.api.request.StatsEvent
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.repository.TelemetryRemoteDataSource
import javax.inject.Inject

public class TelemetryRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : TelemetryRemoteDataSource {
    override suspend fun sendEvents(userId: UserId?, events: List<TelemetryEvent>) {
        val statsEvents = events.map { StatsEvent.fromTelemetryEvent(it) }
        apiProvider.get<TelemetryApi>(userId).invoke {
            postDataMetrics(DataStatsMultipleRequest(statsEvents))
        }.valueOrThrow
    }
}
