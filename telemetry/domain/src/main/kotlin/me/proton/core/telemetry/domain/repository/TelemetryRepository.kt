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

package me.proton.core.telemetry.domain.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent

public interface TelemetryRepository {
    public suspend fun addEvent(userId: UserId?, event: TelemetryEvent)
    public suspend fun deleteAllEvents(userId: UserId?)
    public suspend fun deleteEvents(userId: UserId?, events: List<TelemetryEvent>)
    public suspend fun getEvents(userId: UserId?, limit: Int): List<TelemetryEvent>
    public suspend fun sendEvents(userId: UserId?, events: List<TelemetryEvent>)
}
