/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.eventmanager.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.api.EventApi
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.eventmanager.data.extension.fromEntity
import me.proton.core.eventmanager.data.extension.toEntity
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventIdResponse
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.entity.State
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.network.data.ApiProvider

open class EventMetadataRepositoryImpl(
    db: EventMetadataDatabase,
    private val provider: ApiProvider
) : EventMetadataRepository {

    private val eventMetadataDao = db.eventMetadataDao()

    private suspend fun insertOrUpdate(entity: EventMetadataEntity) =
        eventMetadataDao.insertOrUpdate(entity)

    override fun observe(config: EventManagerConfig): Flow<List<EventMetadata>> =
        eventMetadataDao.observe(config.userId, config).map { it.map { entity -> entity.fromEntity() } }

    override fun observe(config: EventManagerConfig, eventId: EventId): Flow<EventMetadata?> =
        eventMetadataDao.observe(config.userId, config, eventId.id).map { entity -> entity?.fromEntity() }

    override suspend fun delete(config: EventManagerConfig, eventId: EventId) =
        eventMetadataDao.delete(config.userId, config, eventId.id)

    override suspend fun deleteAll(config: EventManagerConfig) =
        eventMetadataDao.deleteAll(config.userId, config)

    override suspend fun update(metadata: EventMetadata) =
        insertOrUpdate(metadata.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun updateState(config: EventManagerConfig, state: State) =
        eventMetadataDao.updateState(config.userId, config, state, System.currentTimeMillis())

    override suspend fun updateState(config: EventManagerConfig, eventId: EventId, state: State) =
        eventMetadataDao.updateState(config.userId, config, eventId.id, state, System.currentTimeMillis())

    override suspend fun get(config: EventManagerConfig): List<EventMetadata> =
        eventMetadataDao.get(config.userId, config).map { it.fromEntity() }

    override suspend fun get(config: EventManagerConfig, eventId: EventId): EventMetadata? =
        eventMetadataDao.get(config.userId, config, eventId.id)?.fromEntity()

    override suspend fun getLatestEventId(
        userId: UserId,
        endpoint: String
    ): EventIdResponse = provider.get<EventApi>(userId).invoke {
        val response = getLatestEventId(endpoint)
        EventIdResponse(response.string())
    }.valueOrThrow

    override suspend fun getEvents(
        userId: UserId,
        eventId: EventId,
        endpoint: String
    ): EventsResponse = provider.get<EventApi>(userId).invoke {
        val response = getEvents(endpoint, eventId.id)
        EventsResponse(response.string())
    }.valueOrThrow
}
