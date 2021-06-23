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

package me.proton.core.eventmanager.domain

import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse

interface EventManager {

    /**
     * [EventManagerConfig] associated with this [EventManager].
     */
    val config: EventManagerConfig

    /**
     * Returns `true` when the Event loop is started.
     *
     * @see start
     * @see stop
     */
    val isStarted: Boolean

    /**
     * Start the Event loop. This call can be called multiple consecutive time without affecting the behavior.
     *
     * Note: The loop will automatically be paused or resumed depending the associated [config] User account state.
     */
    suspend fun start()

    /**
     * Stop the Event loop. This call can be called multiple consecutive time without affecting the behavior.
     *
     * Note: The loop will not automatically be restarted/resumed after this call.
     */
    suspend fun stop()

    /**
     * Pause the Event loop, calls the specified function [block], and resume the loop.
     *
     * Note: The loop will not be paused/resumed if it was not already started.
     */
    suspend fun <R> suspend(block: suspend () -> R): R

    /**
     * Subscribe a new [eventListener].
     */
    fun subscribe(eventListener: EventListener<*, *>)

    /**
     * Process the next task for the associated [config], if exist.
     */
    suspend fun process()

    /**
     * Fetch the latest EventId for the associated [config].
     */
    suspend fun getLatestEventId(): EventId

    /**
     * Fetch the Events for the associated [config] and [eventId].
     */
    suspend fun getEventResponse(eventId: EventId): EventsResponse

    /**
     * Deserialize an [EventMetadata] from [response] for the associated [config] and [eventId].
     */
    suspend fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata
}
