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

package me.proton.core.eventmanager.data.extension

import me.proton.core.eventmanager.data.entity.EventMetadataEntity
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse

fun EventMetadata.toEntity() = EventMetadataEntity(
    userId = userId,
    config = config,
    eventId = eventId?.id,
    nextEventId = nextEventId?.id,
    refresh = refresh,
    more = more,
    retry = retry,
    state = state,
    createdAt = createdAt,
    updatedAt = updatedAt ?: System.currentTimeMillis()
)

fun EventMetadataEntity.fromEntity(response: EventsResponse?) = EventMetadata(
    userId = userId,
    config = config,
    eventId = eventId?.let { EventId(it) },
    nextEventId = nextEventId?.let { EventId(it) },
    refresh = refresh,
    more = more,
    response = response,
    retry = retry,
    state = state,
    createdAt = createdAt,
    updatedAt = updatedAt
)
