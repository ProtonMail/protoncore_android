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

package me.proton.core.mailsettings.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.mailsettings.data.api.response.MailSettingsResponse
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.extension.toMailSettings
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class MailSettingsEvents(
    @SerialName("MailSettings")
    val settings: MailSettingsResponse
)

@Singleton
class MailSettingsEventListener @Inject constructor(
    private val db: MailSettingsDatabase,
    private val repository: MailSettingsRepository
) : EventListener<String, MailSettingsResponse>() {

    override val type = Type.Core
    override val order = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, MailSettingsResponse>>? {
        return response.body.deserializeOrNull<MailSettingsEvents>()?.let {
            listOf(Event(Action.Update, "null", it.settings))
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R {
        return db.inTransaction(block)
    }

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<MailSettingsResponse>) {
        repository.updateMailSettings(entities.first().toMailSettings(config.userId))
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        repository.getMailSettings(config.userId, refresh = true)
    }
}
