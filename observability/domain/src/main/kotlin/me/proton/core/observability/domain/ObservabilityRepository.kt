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

import me.proton.core.observability.domain.entity.ObservabilityEvent

public interface ObservabilityRepository {
    public suspend fun addEvent(event: ObservabilityEvent)
    public suspend fun deleteAllEvents()
    public suspend fun deleteEvents(events: List<ObservabilityEvent>)
    public suspend fun deleteEvent(event: ObservabilityEvent)
    public suspend fun getEventsAndSanitizeDb(limit: Int? = null): List<ObservabilityEvent>
    public suspend fun getEventCount(): Long
}
