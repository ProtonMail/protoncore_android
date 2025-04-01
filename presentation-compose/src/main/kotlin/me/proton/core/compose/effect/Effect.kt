/*
 * Copyright (c) 2025 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.compose.effect

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ConsistentCopyVisibility
data class Effect<T : Any> private constructor(private var event: T?) {
    private val mutex = Mutex()

    suspend fun <R> consume(block: suspend (T) -> R): R? = mutex.withLock {
        val event = event ?: return null
        this.event = null
        return block(event)
    }

    suspend fun peek(): T? = mutex.withLock { event }

    companion object {
        fun <T : Any> of(event: T): Effect<T> = Effect(event)
        fun <T : Any> empty(): Effect<T> = Effect(null)
    }
}
