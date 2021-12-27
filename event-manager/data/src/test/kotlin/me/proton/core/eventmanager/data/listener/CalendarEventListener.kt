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

package me.proton.core.eventmanager.data.listener

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.util.kotlin.deserializeOrNull

@Serializable
data class CalendarsEvents(
    @SerialName("Calendars")
    val calendars: List<CalendarEvent>
)

@Serializable
data class CalendarEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("Calendar")
    val calendar: CalendarResource? = null
)

@Serializable
data class CalendarResource(
    @SerialName("ID")
    val id: String
)

open class CalendarEventListener : EventListener<String, CalendarResource>() {

    override val type = Type.Calendar
    override val order = 1

    lateinit var config: EventManagerConfig

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, CalendarResource>>? {
        val events = response.body.deserializeOrNull<CalendarsEvents>()
        return events?.calendars?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, it.calendar)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()

    override suspend fun onPrepare(config: EventManagerConfig, entities: List<CalendarResource>) {
        super.onPrepare(config, entities)
    }

    override suspend fun onCreate(config: EventManagerConfig, entities: List<CalendarResource>) {
        this.config = config
    }
}
