/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.domain.arch.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Emit sequentially all [Entity] that doesn't match the [equalPredicate] between consecutive [List] of [Entity]
 * emission of the underlining [Flow].
 *
 * Example of usage:
 * ```
 * enum class State { New, Dirty, Clean }
 * data class Entity(val id: Int, val state: State)
 *
 * val flowOfEntityList = flowOf(
 *     listOf(Entity(1, New), Entity(2, New)),
 *     listOf(Entity(1, Dirty), Entity(2, New)),
 *     listOf(Entity(1, Clean), Entity(2, New)),
 * )
 *
 * val entityStateChangedFlow = flowOfEntityList.onEntityChanged(
 *     getEntityKey = { it.id },
 *     equalPredicate = { old, new -> old.state == new.state },
 *     emitNewEntity = false
 * ) // Produces: Entity(1, Dirty), Entity(1, Clean).
 * ```
 * @param Entity the list type for the underlining [Flow] of [List].
 * @param Key a key or identifier for an [Entity] - must have a reliable [hashCode] function.
 * @param getEntityKey a function returning the [Key] for an [Entity].
 * @param equalPredicate a function providing equal predicate between two [Entity].
 * @param emitNewEntity if true, the first state of an [Entity] will also be emitted initially.
 */
fun <Entity, Key> Flow<List<Entity>>.onEntityChanged(
    getEntityKey: (Entity) -> Key,
    equalPredicate: (old: Entity, new: Entity) -> Boolean = { old, new -> old == new },
    emitNewEntity: Boolean = true
): Flow<Entity> = flow {
    val map = ConcurrentHashMap<Key, Entity>()
    collect { newList ->
        val newKeySet = mutableSetOf<Key>()

        newList.forEach { new ->
            val key = getEntityKey(new)
            newKeySet.add(key)
            val old = map[key]
            val hasChanged = if (old != null) !equalPredicate(old, new) else emitNewEntity
            if (hasChanged) emit(new)
            map[key] = new
        }

        val oldKeys = map.filterKeys { !newKeySet.contains(it) }
        oldKeys.forEach { map.remove(it.key) }
    }
}
