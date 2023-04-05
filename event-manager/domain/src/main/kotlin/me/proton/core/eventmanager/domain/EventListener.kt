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

import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.extension.groupByAction
import me.proton.core.util.kotlin.takeIfNotEmpty

abstract class EventListener<K : Any, T : Any> : TransactionHandler {

    private val actionMapByConfig = mutableMapOf<EventManagerConfig, Map<Action, List<Event<K, T>>>>()
    private val eventMetadataByConfig = mutableMapOf<EventManagerConfig, EventMetadata>()

    /**
     * Type of Event loop.
     */
    enum class Type {
        /**
         * Core Event loop.
         *
         * Contains: Messages, Conversations, Import, Contacts, Filter, Labels, Subscriptions, User, Settings, ...
         */
        Core,

        /**
         * Calendar Event loop.
         *
         * Contains: Calendars, CalendarKeys, CalendarEvents, CalendarAlarms, Settings, CalendarSubscriptions, ...
         */
        Calendar,

        /**
         * Drive Event loop.
         *
         * Contains: Share, Links, Nodes, ...
         */
        Drive
    }

    /**
     * Listener [type] to associate with this [EventListener].
     */
    abstract val type: Type

    /**
     * The degrees of separation from this entity to the User entity.
     *
     * Examples:
     * - UserEventListener: User => 0.
     * - UserAddressEventListener: UserAddress -> User => 1.
     * - ContactEventListener: Contact -> User => 1.
     * - ContactEmailEventListener: ContactEmail -> Contact -> User => 2.
     */
    abstract val order: Int

    private suspend fun setMetadata(config: EventManagerConfig, metadata: EventMetadata) {
        val events = metadata.response?.let { deserializeEvents(config, it) }.orEmpty()
        actionMapByConfig[config] = events.groupByAction()
        eventMetadataByConfig[config] = metadata
    }

    private fun clearMetadata(config: EventManagerConfig) {
        actionMapByConfig.remove(config)
        eventMetadataByConfig.remove(config)
    }

    /**
     * Get actions part of the current set of modifications.
     *
     * Note: The map is created in first place just before [onPrepare], and cleared after [onComplete].
     */
    fun getActionMap(config: EventManagerConfig): Map<Action, List<Event<K, T>>> =
        actionMapByConfig.getOrPut(config) { emptyMap() }

    /**
     * Get [EventMetadata] part of the current set of modifications.
     *
     * Note: The map is created in first place just before [onPrepare], and cleared after [onComplete].
     */
    fun getEventMetadata(config: EventManagerConfig): EventMetadata =
        eventMetadataByConfig.getValue(config)

    /**
     * Notify to prepare any additional data (e.g. foreign key).
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyPrepare(config: EventManagerConfig, metadata: EventMetadata) {
        requireNotNull(metadata.response)
        setMetadata(config, metadata)
        val actions = getActionMap(config)
        val entities = actions[Action.Create].orEmpty() + actions[Action.Update].orEmpty()
        entities.takeIfNotEmpty()?.let { list -> onPrepare(config, list.mapNotNull { it.entity }) }
    }

    /**
     * Notify all events in this order [onCreate], [onUpdate], [onPartial] and [onDelete].
     *
     * Note: A transaction wraps this function.
     */
    suspend fun notifyEvents(config: EventManagerConfig, metadata: EventMetadata) {
        requireNotNull(metadata.response)
        setMetadata(config, metadata)
        val actions = getActionMap(config)
        actions[Action.Create]?.takeIfNotEmpty()?.let { list -> onCreate(config, list.mapNotNull { it.entity }) }
        actions[Action.Update]?.takeIfNotEmpty()?.let { list -> onUpdate(config, list.mapNotNull { it.entity }) }
        actions[Action.Partial]?.takeIfNotEmpty()?.let { list -> onPartial(config, list.mapNotNull { it.entity }) }
        actions[Action.Delete]?.takeIfNotEmpty()?.let { list -> onDelete(config, list.map { it.key }) }
    }

    /**
     * Notify to reset all entities.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyResetAll(config: EventManagerConfig, metadata: EventMetadata) {
        setMetadata(config, metadata)
        onResetAll(config)
    }

    /**
     * Notify successful completion of applying the set of modifications.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifySuccess(config: EventManagerConfig, metadata: EventMetadata) {
        requireNotNull(metadata.response)
        setMetadata(config, metadata)
        onSuccess(config)
    }

    /**
     * Notify failure of applying the set of modifications.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyFailure(config: EventManagerConfig, metadata: EventMetadata) {
        setMetadata(config, metadata)
        onFailure(config)
    }

    /**
     * Notify complete, whether applying the set of modifications is a success or a failure.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyComplete(config: EventManagerConfig, metadata: EventMetadata) {
        setMetadata(config, metadata)
        onComplete(config)
        clearMetadata(config)
    }

    /**
     * Deserialize [response] into a typed list of [Event].
     */
    abstract suspend fun deserializeEvents(config: EventManagerConfig, response: EventsResponse): List<Event<K, T>>?

    /**
     * Called before applying a set of modifications to prepare any additional action (e.g. fetch foreign entities).
     *
     * Note: Delete action entities are filtered out.
     *
     * @see onComplete
     */
    open suspend fun onPrepare(config: EventManagerConfig, entities: List<T>) {}

    /**
     * Called to created entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onCreate(config: EventManagerConfig, entities: List<T>) {}

    /**
     * Called to update or insert entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onUpdate(config: EventManagerConfig, entities: List<T>) {}

    /**
     * Called to partially update entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onPartial(config: EventManagerConfig, entities: List<T>) {}

    /**
     * Called to delete, if exist, entities in persistence.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     */
    open suspend fun onDelete(config: EventManagerConfig, keys: List<K>) {}

    /**
     * Called to reset/delete all entities in persistence, for this type.
     *
     * Note: Usually a good time the fetch minimal data after deletion.
     */
    open suspend fun onResetAll(config: EventManagerConfig) {}

    /**
     * Called after successfully applying the set of modifications.
     *
     * Note: [onComplete] will be called right after.
     *
     * @see onComplete
     */
    open suspend fun onSuccess(config: EventManagerConfig) {}

    /**
     * Called after failing applying the set of modifications.
     *
     * Note: [onResetAll] will be called right after.
     *
     * @see onResetAll
     */
    open suspend fun onFailure(config: EventManagerConfig) {}

    /**
     * Called as a final step, whether it is a success or a failure.
     *
     * Note: It can be used to perform any final cleanup before processing the next set of modifications.
     *       Do not count on this method being called as a place for saving data, there is no retry logic here.
     *       For those use cases, you should rely on [onSuccess] or on [onFailure], not here.
     *
     * @see onSuccess
     * @see onFailure
     */
    open suspend fun onComplete(config: EventManagerConfig) {}
}
