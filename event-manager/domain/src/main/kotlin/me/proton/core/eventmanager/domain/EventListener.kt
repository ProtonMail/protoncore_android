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
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.extension.groupByAction
import me.proton.core.util.kotlin.takeIfNotEmpty

abstract class EventListener<Key : Any, Type : Any> : TransactionHandler {

    private val actionMapByConfig = mutableMapOf<EventManagerConfig, Map<Action, List<Event<Key, Type>>>>()

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
    abstract val type: EventListener.Type

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

    /**
     * Get actions part of the current set of modifications.
     *
     * Note: The map is created just before [onPrepare], and cleared after [onComplete].
     */
    fun getActionMap(config: EventManagerConfig): Map<Action, List<Event<Key, Type>>> {
        return actionMapByConfig.getOrPut(config) { emptyMap() }
    }

    /**
     * Set the actions part of the current set of modifications.
     *
     * Note: Called before [notifyPrepare] and after [notifyComplete].
     */
    fun setActionMap(config: EventManagerConfig, events: List<Event<Key, Type>>) {
        actionMapByConfig[config] = events.groupByAction()
    }

    /**
     * Notify to prepare any additional data (e.g. foreign key).
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyPrepare(config: EventManagerConfig) {
        val actions = getActionMap(config)
        val entities = actions[Action.Create].orEmpty() + actions[Action.Update].orEmpty()
        entities.takeIfNotEmpty()?.let { list -> onPrepare(config, list.mapNotNull { it.entity }) }
    }

    /**
     * Notify all events in this order [onCreate], [onUpdate], [onPartial] and [onDelete].
     *
     * Note: A transaction wraps this function.
     */
    suspend fun notifyEvents(config: EventManagerConfig) {
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
    suspend fun notifyResetAll(config: EventManagerConfig) {
        onResetAll(config)
    }

    /**
     * Notify complete, whether set of modifications was successful or not.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyComplete(config: EventManagerConfig) {
        onComplete(config)
        setActionMap(config, emptyList())
    }

    /**
     * Notify successful completion of the [EventListener]'s loop. Called before [onComplete].
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifySuccess(config: EventManagerConfig) = onSuccess(config)

    /**
     * Notify completion with failures of the [EventListener]'s loop. Called before [onComplete] and after [onResetAll].
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyFailure(config: EventManagerConfig) = onFailure(config)

    /**
     * Deserialize [response] into a typed list of [Event].
     */
    abstract suspend fun deserializeEvents(config: EventManagerConfig, response: EventsResponse): List<Event<Key, Type>>?

    /**
     * Called before applying a set of modifications to prepare any additional action (e.g. fetch foreign entities).
     *
     * Note: Delete action entities are filtered out.
     *
     * @see onComplete
     */
    open suspend fun onPrepare(config: EventManagerConfig, entities: List<Type>) = Unit

    /**
     * Called at the end of a set of modifications, whether it is successful or not. It can be used to clean up any
     * resources that are no longer needed.
     *
     * @see onPrepare
     */
    open suspend fun onComplete(config: EventManagerConfig) = Unit

    /**
     * Called at the end of a set of modifications when it is successful.
     *
     * Note: [onComplete] will be called right after.
     *
     * @see onComplete
     */
    open suspend fun onSuccess(config: EventManagerConfig) = Unit

    /**
     * Called at the end of a set of modifications after it failed.
     *
     * Note: [onComplete] will be called right after.
     *
     * @see onComplete
     */
    open suspend fun onFailure(config: EventManagerConfig) = Unit

    /**
     * Called to created entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onCreate(config: EventManagerConfig, entities: List<Type>) = Unit

    /**
     * Called to update or insert entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onUpdate(config: EventManagerConfig, entities: List<Type>) = Unit

    /**
     * Called to partially update entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onPartial(config: EventManagerConfig, entities: List<Type>) = Unit

    /**
     * Called to delete, if exist, entities in persistence.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     */
    open suspend fun onDelete(config: EventManagerConfig, keys: List<Key>) = Unit

    /**
     * Called to reset/delete all entities in persistence, for this type.
     *
     * Note: Usually a good time the fetch minimal data after deletion.
     */
    open suspend fun onResetAll(config: EventManagerConfig) = Unit
}
