/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.eventmanager.domain.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventIdResponse
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.entity.State

interface EventMetadataRepository {

    suspend fun deleteAll(config: EventManagerConfig)
    suspend fun delete(config: EventManagerConfig, eventId: EventId)

    suspend fun update(metadata: EventMetadata, response: EventsResponse)
    suspend fun updateMetadata(metadata: EventMetadata)
    suspend fun updateEventId(config: EventManagerConfig, oldEventId: EventId?, newEventId: EventId)
    suspend fun updateNextEventId(config: EventManagerConfig, eventId: EventId?, nextEventId: EventId)
    suspend fun updateState(config: EventManagerConfig, state: State)
    suspend fun updateState(config: EventManagerConfig, eventId: EventId, state: State)
    suspend fun updateRetry(config: EventManagerConfig, retry: Int)

    suspend fun getAll(userId: UserId): List<EventMetadata>
    suspend fun get(config: EventManagerConfig): List<EventMetadata>
    suspend fun get(config: EventManagerConfig, eventId: EventId): EventMetadata?
    suspend fun getEvents(config: EventManagerConfig, eventId: EventId): EventsResponse?

    suspend fun getLatestEventId(userId: UserId, endpoint: String): EventIdResponse
    suspend fun getEvents(userId: UserId, eventId: EventId, endpoint: String): EventsResponse
}
