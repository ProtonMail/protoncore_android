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

package me.proton.core.usersettings.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.extension.toUserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserSettingsEvents(
    @SerialName("UserSettings")
    val settings: UserSettingsResponse? = null
)

@Singleton
open class UserSettingsEventListener @Inject constructor(
    private val db: UserSettingsDatabase,
    private val repository: UserSettingsRepository
) : EventListener<String, UserSettingsResponse>() {

    override val type = Type.Core
    override val order = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, UserSettingsResponse>>? {
        return response.body.deserialize<UserSettingsEvents>().settings?.let {
            listOf(Event(Action.Update, "null", it))
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.inTransaction(block)

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<UserSettingsResponse>) {

        repository.updateUserSettings(entities.first().toUserSettings(config.userId))
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        repository.getUserSettings(config.userId, refresh = true)
    }
}
