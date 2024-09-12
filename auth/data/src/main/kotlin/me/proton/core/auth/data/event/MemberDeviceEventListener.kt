/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.data.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.data.api.response.MemberDeviceResource
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.auth.domain.repository.MemberDeviceLocalDataSource
import me.proton.core.auth.domain.repository.MemberDeviceRepository
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject

class MemberDeviceEventListener @Inject constructor(
    private val authDatabase: AuthDatabase,
    private val memberDeviceLocalDataSource: MemberDeviceLocalDataSource,
    private val memberDeviceRepository: MemberDeviceRepository
) : EventListener<String, MemberDeviceResource>() {
    override val type: Type = Type.Core
    override val order: Int = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, MemberDeviceResource>>? {
        return response.body.deserialize<MemberDeviceEvents>().devices?.map {
            Event(
                requireNotNull(Action.map[it.action]),
                it.id,
                it.device
            )
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = authDatabase.inTransaction(block)

    override suspend fun onCreate(config: EventManagerConfig, entities: List<MemberDeviceResource>) {
        memberDeviceLocalDataSource.upsert(entities.map { it.toMemberDevice(config.userId) })
    }

    override suspend fun onDelete(config: EventManagerConfig, keys: List<String>) {
        memberDeviceLocalDataSource.deleteByDeviceId(config.userId, keys.map { MemberDeviceId(it) })
    }

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<MemberDeviceResource>) {
        memberDeviceLocalDataSource.upsert(entities.map { it.toMemberDevice(config.userId) })
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        memberDeviceLocalDataSource.deleteAll(config.userId)
        memberDeviceRepository.getByUserId(config.userId, refresh = true)
    }
}

@Serializable
private data class MemberDeviceEvents(
    @SerialName("MemberAuthDevices")
    val devices: List<MemberDeviceEvent>? = null
)

@Serializable
private data class MemberDeviceEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("MemberAuthDevice")
    val device: MemberDeviceResource? = null
)
