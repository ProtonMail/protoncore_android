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

package me.proton.core.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.data.api.response.AuthDeviceResponse
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.repository.AuthDeviceLocalDataSource
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AuthDevicesEvents(
    @SerialName("AuthDevices")
    val authDevices: List<AuthDevicesEvent>? = null
)

@Serializable
data class AuthDevicesEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("AuthDevice")
    val authDevice: AuthDeviceResponse? = null
)

@Singleton
class AuthDeviceEventListener @Inject constructor(
    private val db: AuthDatabase,
    private val authDeviceLocalDataSource: AuthDeviceLocalDataSource,
    private val authDeviceRepository: AuthDeviceRepository
): EventListener<String, AuthDeviceResponse>() {

    override val type = Type.Core
    override val order = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, AuthDeviceResponse>>? {
        return response.body.deserialize<AuthDevicesEvents>().authDevices?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, it.authDevice)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.inTransaction(block)

    override suspend fun onCreate(config: EventManagerConfig, entities: List<AuthDeviceResponse>) {
        authDeviceLocalDataSource.upsert(entities.map { it.toAuthDevice(config.userId) })
    }

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<AuthDeviceResponse>) {
        authDeviceLocalDataSource.upsert(entities.map { it.toAuthDevice(config.userId) })
    }

    override suspend fun onDelete(config: EventManagerConfig, keys: List<String>) {
        authDeviceLocalDataSource.deleteByDeviceId(*keys.map { AuthDeviceId(it) }.toTypedArray())
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        authDeviceRepository.getByUserId(config.userId, refresh = true)
    }
}
