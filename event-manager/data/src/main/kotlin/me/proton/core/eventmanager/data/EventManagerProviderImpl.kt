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

package me.proton.core.eventmanager.data

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerConfigProvider
import me.proton.core.eventmanager.domain.EventManagerProvider
import javax.inject.Singleton

@Singleton
class EventManagerProviderImpl(
    private val eventManagerFactory: EventManagerFactory,
    private val eventManagerConfigProvider: EventManagerConfigProvider,
    @JvmSuppressWildcards
    private val eventListeners: Set<EventListener<*, *>>
) : EventManagerProvider {

    // 1 EventManager instance per Config.
    private val managers = mutableMapOf<EventManagerConfig, EventManager>()
    private val eventListenersByType = eventListeners.groupBy { it.type }

    override fun get(config: EventManagerConfig): EventManager {
        // Only create a new instance if config is not found.
        return managers.getOrPut(config) {
            val deserializer = when (config) {
                is EventManagerConfig.Core -> CoreEventDeserializer(config)
                is EventManagerConfig.Calendar -> CalendarEventDeserializer(config)
                is EventManagerConfig.Drive -> DriveEventDeserializer(config)
            }
            // Create a new EventManager associated with this config.
            eventManagerFactory.create(deserializer).apply {
                // Subscribe all known Listener for the same type to it.
                eventListenersByType[config.listenerType]?.forEach { subscribe(it) }
            }
        }
    }

    override suspend fun getAll(userId: UserId): List<EventManager> =
        eventManagerConfigProvider.getAll(userId).map { get(it) }
}
