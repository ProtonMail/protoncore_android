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

import me.proton.core.eventmanager.data.api.response.GetCalendarEventsResponse
import me.proton.core.eventmanager.data.api.response.GetCalendarLatestEventIdResponse
import me.proton.core.eventmanager.data.api.response.GetCoreEventsResponse
import me.proton.core.eventmanager.data.api.response.GetCoreLatestEventIdResponse
import me.proton.core.eventmanager.data.api.response.GetDriveEventsResponse
import me.proton.core.eventmanager.data.api.response.GetDriveLatestEventIdResponse
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventIdResponse
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.entity.RefreshType
import me.proton.core.util.kotlin.deserialize

interface EventDeserializer {
    val config: EventManagerConfig
    val endpoint: String
    fun deserializeLatestEventId(response: EventIdResponse): EventId
    fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata
}

internal data class CoreEventDeserializer(
    override val config: EventManagerConfig.Core
) : EventDeserializer {

    override val endpoint = "core/v4/events"

    override fun deserializeLatestEventId(response: EventIdResponse): EventId =
        EventId(response.body.deserialize<GetCoreLatestEventIdResponse>().eventId)

    override fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata =
        response.body.deserialize<GetCoreEventsResponse>().let {
            EventMetadata(
                userId = config.userId,
                eventId = eventId,
                config = config,
                nextEventId = EventId(it.eventId),
                refresh = RefreshType.mapByValue[it.refresh],
                more = it.more > 0,
                response = response,
                createdAt = System.currentTimeMillis()
            )
        }
}

internal data class CalendarEventDeserializer(
    override val config: EventManagerConfig.Calendar
) : EventDeserializer {

    override val endpoint = "calendar/${config.apiVersion}/${config.calendarId}/modelevents"

    override fun deserializeLatestEventId(response: EventIdResponse): EventId =
        EventId(response.body.deserialize<GetCalendarLatestEventIdResponse>().eventId)

    override fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata =
        response.body.deserialize<GetCalendarEventsResponse>().let {
            EventMetadata(
                userId = config.userId,
                eventId = eventId,
                config = config,
                nextEventId = EventId(it.eventId),
                refresh = RefreshType.mapByValue[it.refresh],
                more = it.more > 0,
                response = response,
                createdAt = System.currentTimeMillis()
            )
        }
}

internal class DriveEventDeserializer(
    override val config: EventManagerConfig.Drive
) : EventDeserializer {

    override val endpoint = "drive/shares/${config.shareId}/events"

    override fun deserializeLatestEventId(response: EventIdResponse): EventId =
        EventId(response.body.deserialize<GetDriveLatestEventIdResponse>().eventId)

    override fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata =
        response.body.deserialize<GetDriveEventsResponse>().let {
            EventMetadata(
                userId = config.userId,
                eventId = eventId,
                config = config,
                nextEventId = EventId(it.eventId),
                refresh = RefreshType.mapByValue[it.refresh],
                more = it.more > 0,
                response = response,
                createdAt = System.currentTimeMillis()
            )
        }
}
