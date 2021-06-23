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

package me.proton.core.eventmanager.domain.entity

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig

data class EventMetadata(
    val userId: UserId,
    val eventId: EventId?,
    val config: EventManagerConfig,
    val nextEventId: EventId? = null,
    val refresh: RefreshType? = null,
    val more: Boolean? = null,
    val response: EventsResponse? = null,
    val retry: Int = 0,
    val state: State = State.Enqueued,
    val createdAt: Long,
    val updatedAt: Long? = null
)

enum class RefreshType(val value: Int) {
    Nothing(0),
    Mail(1),
    Contact(2),
    All(255);

    companion object {
        val map = values().associateBy { it.value }
    }
}

enum class State(val value: Int) {
    Enqueued(0),
    Fetching(1),
    Persisted(2),
    NotifyPrepare(3),
    NotifyEvents(4),
    NotifyResetAll(5),
    NotifyComplete(6),
    Completed(7),
}
