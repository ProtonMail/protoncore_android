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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.data.file.AndroidFileContext
import me.proton.core.data.file.ExperimentalProtonFileContext
import me.proton.core.data.file.FileContext
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.EventManagerQueryMapProvider
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
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalProtonFileContext::class)
open class EventMetadataRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val db: EventMetadataDatabase,
    private val apiProvider: ApiProvider,
    private val eventManagerQueryMapProvider: Optional<EventManagerQueryMapProvider>
) : EventMetadataRepository,
    FileContext<EventManagerConfig, EventId> by AndroidFileContext("events", context) {

    private val eventMetadataDao = db.eventMetadataDao()

    private suspend fun readResponse(
        config: EventManagerConfig,
        eventId: EventId?
    ) = eventId?.let { readText(config, it)?.let { text -> EventsResponse(text) } }

    private suspend fun writeResponse(
        config: EventManagerConfig,
        eventId: EventId?,
        response: EventsResponse?
    ) = eventId?.let { id -> response?.body?.let { data -> writeText(config, id, data) } }

    private suspend fun deleteResponse(
        config: EventManagerConfig,
        eventId: EventId?
    ) = eventId?.let { deleteText(config, it) }

    private suspend fun insertOrUpdate(entity: EventMetadataEntity) =
        eventMetadataDao.insertOrUpdate(entity)

    override suspend fun delete(config: EventManagerConfig, eventId: EventId) {
        deleteResponse(config, eventId)
        eventMetadataDao.delete(config.userId, config, eventId.id)
    }

    override suspend fun deleteAll(config: EventManagerConfig) {
        deleteDir(config)
        eventMetadataDao.deleteAll(config.userId, config)
    }

    override suspend fun update(metadata: EventMetadata, response: EventsResponse) {
        deleteDir(metadata.config)
        writeResponse(metadata.config, metadata.eventId, response)
        insertOrUpdate(metadata.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateMetadata(metadata: EventMetadata) {
        insertOrUpdate(metadata.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateEventId(
        config: EventManagerConfig,
        oldEventId: EventId?,
        newEventId: EventId
    ) = eventMetadataDao.updateEventId(config.userId, config, oldEventId?.id, newEventId.id)

    override suspend fun updateNextEventId(
        config: EventManagerConfig,
        eventId: EventId?,
        nextEventId: EventId
    ) = eventMetadataDao.updateNextEventId(config.userId, config, eventId?.id, nextEventId.id)

    override suspend fun updateState(
        config: EventManagerConfig,
        state: State
    ) = eventMetadataDao.updateState(config.userId, config, state, System.currentTimeMillis())

    override suspend fun updateState(
        config: EventManagerConfig,
        eventId: EventId,
        state: State
    ) = eventMetadataDao.updateState(config.userId, config, eventId.id, state, System.currentTimeMillis())

    override suspend fun updateRetry(
        config: EventManagerConfig,
        retry: Int
    ) = eventMetadataDao.updateRetry(config.userId, config, retry, System.currentTimeMillis())

    override suspend fun getAll(
        userId: UserId
    ): List<EventMetadata> = eventMetadataDao.getAll(userId).map { it.fromEntity() }

    override suspend fun get(
        config: EventManagerConfig
    ): List<EventMetadata> = eventMetadataDao.get(config.userId, config).map { it.fromEntity() }

    override suspend fun get(
        config: EventManagerConfig,
        eventId: EventId
    ): EventMetadata? = eventMetadataDao.get(config.userId, config, eventId.id)?.fromEntity()

    override suspend fun getEvents(
        config: EventManagerConfig,
        eventId: EventId
    ): EventsResponse? = readResponse(config, eventId)

    override suspend fun getLatestEventId(
        userId: UserId,
        endpoint: String
    ): EventIdResponse = apiProvider.get<EventApi>(userId).invoke {
        val response = getLatestEventId(endpoint)
        EventIdResponse(response.string())
    }.valueOrThrow

    override suspend fun getEvents(
        config: EventManagerConfig,
        eventId: EventId,
        endpoint: String
    ): EventsResponse = apiProvider.get<EventApi>(config.userId).invoke {
        val response = getEvents(endpoint, eventId.id, eventManagerQueryMapProvider.getOrNull()?.getQueryMap(config))
        EventsResponse(response.string())
    }.valueOrThrow
}
