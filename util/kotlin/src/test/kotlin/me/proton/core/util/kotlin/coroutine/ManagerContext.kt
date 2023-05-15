/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.util.kotlin.coroutine

data class Event(val key: String, val value: Int)

interface Manager {
    fun enqueue(event: Event)
}

interface ManagerContext {

    val manager: Manager

    fun enqueue(event: Event): Unit = manager.enqueue(event)

    fun <T> Result<T>.enqueue(
        block: Result<T>.() -> Event
    ): Result<T> = also { enqueue(block(this)) }

    suspend fun <T> ResultCollector<T>.onResultEnqueue(
        key: String,
        block: Result<T>.() -> Event
    ): Unit = onResult(key) { enqueue(block) }

    suspend fun <T> ResultCollector<T>.onCompleteEnqueue(
        block: Result<T>.() -> Event
    ): Unit = onComplete { enqueue(block) }
}
