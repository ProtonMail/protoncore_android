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

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.extension.groupByAction
import me.proton.core.util.kotlin.takeIfNotEmpty

abstract class EventListener<K, T : Any> : TransactionHandler {

    private val actionMapByUserId = mutableMapOf<UserId, Map<Action, List<Event<K, T>>>>()

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

    /**
     * Get actions part of the current set of modifications.
     *
     * Note: The map is created just before [onPrepare], and cleared after [onComplete].
     */
    fun getActionMap(userId: UserId): Map<Action, List<Event<K, T>>> {
        return actionMapByUserId.getOrPut(userId) { emptyMap() }
    }

    /**
     * Set the actions part of the current set of modifications.
     *
     * Note: Called before [notifyPrepare] and after [notifyComplete].
     */
    fun setActionMap(userId: UserId, events: List<Event<K, T>>) {
        actionMapByUserId[userId] = events.groupByAction()
    }

    /**
     * Notify to prepare any additional data (e.g. foreign key).
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyPrepare(userId: UserId) {
        val actions = getActionMap(userId)
        val entities = actions[Action.Create].orEmpty() + actions[Action.Update].orEmpty()
        entities.takeIfNotEmpty()?.let { list -> onPrepare(userId, list.mapNotNull { it.entity }) }
    }

    /**
     * Notify all events in this order [onCreate], [onUpdate], [onPartial] and [onDelete].
     *
     * Note: A transaction wraps this function.
     */
    suspend fun notifyEvents(userId: UserId) {
        val actions = getActionMap(userId)
        actions[Action.Create]?.takeIfNotEmpty()?.let { list -> onCreate(userId, list.mapNotNull { it.entity }) }
        actions[Action.Update]?.takeIfNotEmpty()?.let { list -> onUpdate(userId, list.mapNotNull { it.entity }) }
        actions[Action.Partial]?.takeIfNotEmpty()?.let { list -> onPartial(userId, list.mapNotNull { it.entity }) }
        actions[Action.Delete]?.takeIfNotEmpty()?.let { list -> onDelete(userId, list.map { it.key }) }
    }

    /**
     * Notify to reset all entities.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyResetAll(userId: UserId) {
        onResetAll(userId)
    }

    /**
     * Notify complete, whether set of modifications was successful or not.
     *
     * Note: No transaction wraps this function.
     */
    suspend fun notifyComplete(userId: UserId) {
        onComplete(userId)
        setActionMap(userId, emptyList())
    }

    /**
     * Deserialize [response] into a typed list of [Event].
     */
    abstract suspend fun deserializeEvents(response: EventsResponse): List<Event<K, T>>?

    /**
     * Called before applying a set of modifications to prepare any additional action (e.g. fetch foreign entities).
     *
     * Note: Delete action entities are filtered out.
     *
     * @see onComplete
     */
    open suspend fun onPrepare(userId: UserId, entities: List<T>) = Unit

    /**
     * Called at the end of a set of modifications, whether it is successful or not.
     *
     * @see onPrepare
     */
    open suspend fun onComplete(userId: UserId) = Unit

    /**
     * Called to created entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onCreate(userId: UserId, entities: List<T>) = Unit

    /**
     * Called to update or insert entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onUpdate(userId: UserId, entities: List<T>) = Unit

    /**
     * Called to partially update entities in persistence.
     *
     * There is no guarantee any prior [onCreate] will be called for any needed foreign entity.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     *
     * @see onPrepare
     */
    open suspend fun onPartial(userId: UserId, entities: List<T>) = Unit

    /**
     * Called to delete, if exist, entities in persistence.
     *
     * Note: A transaction wraps this function and must return as fast as possible.
     */
    open suspend fun onDelete(userId: UserId, keys: List<K>) = Unit

    /**
     * Called to reset/delete all entities in persistence, for this type.
     *
     * Note: Usually a good time the fetch minimal data after deletion.
     */
    open suspend fun onResetAll(userId: UserId) = Unit
}
