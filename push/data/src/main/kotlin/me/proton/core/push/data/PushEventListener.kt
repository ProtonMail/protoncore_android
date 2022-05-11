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

package me.proton.core.push.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.data.remote.resource.PushResource
import me.proton.core.push.data.remote.resource.toPush
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.local.PushLocalDataSource
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject

public open class PushEventListener @Inject constructor(
    private val pushDatabase: PushDatabase,
    private val pushLocalDataSource: PushLocalDataSource,
    private val pushRepository: PushRepository
) : EventListener<String, Push>() {

    override val type: Type = Type.Core
    override val order: Int = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, Push>>? {
        return response.body.deserializeOrNull<PushesEvent>()?.pushes?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, it.push?.toPush(config.userId))
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = pushDatabase.inTransaction(block)

    override suspend fun onCreate(config: EventManagerConfig, entities: List<Push>) {
        pushLocalDataSource.upsertPushes(*entities.toTypedArray())
    }

    override suspend fun onDelete(config: EventManagerConfig, keys: List<String>) {
        pushLocalDataSource.deletePushesById(config.userId, *keys.map { PushId(it) }.toTypedArray())
    }

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<Push>) {
        pushLocalDataSource.upsertPushes(*entities.toTypedArray())
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        pushLocalDataSource.deletePushesByUser(config.userId)

        PushObjectType.values().forEach { type ->
            pushRepository.getAllPushes(config.userId, type, refresh = true)
        }
    }
}

@Serializable
private data class PushesEvent(
    @SerialName("Pushes")
    val pushes: List<PushEvent>
)

@Serializable
private data class PushEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("Push")
    val push: PushResource? = null
)
