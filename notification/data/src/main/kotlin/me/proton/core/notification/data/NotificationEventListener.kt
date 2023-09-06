/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.notification.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.remote.response.NotificationResponse
import me.proton.core.notification.data.remote.response.toNotification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
public data class NotificationEvents(
    @SerialName("Notifications")
    val notifications: List<NotificationResponse>? = null
)

@Singleton
public open class NotificationEventListener @Inject constructor(
    private val db: NotificationDatabase,
    private val notificationRepository: NotificationRepository
) : EventListener<String, NotificationResponse>() {

    override val type: Type = Type.Core
    override val order: Int = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, NotificationResponse>>? {
        return response.body.deserialize<NotificationEvents>().notifications?.map {
            Event(requireNotNull(Action.Update), it.notificationId, it)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.inTransaction(block)

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<NotificationResponse>) {
        notificationRepository.upsertNotifications(*entities.map { it.toNotification(config.userId) }.toTypedArray())
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        notificationRepository.deleteAllNotificationsByUser(config.userId)
    }
}
