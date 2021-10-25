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

package me.proton.core.user.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.key.data.api.response.UserResponse
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserEvents(
    @SerialName("User")
    val user: UserResponse
)

@Singleton
class UserEventListener @Inject constructor(
    private val db: UserDatabase,
    private val userRepository: UserRepository
) : EventListener<String, UserResponse>() {

    override val type = Type.Core
    override val order = 0

    override suspend fun deserializeEvents(response: EventsResponse): List<Event<String, UserResponse>>? {
        return response.body.deserializeOrNull<UserEvents>()?.let {
            listOf(Event(Action.Update, it.user.id, it.user))
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R {
        return db.inTransaction(block)
    }

    override suspend fun onUpdate(userId: UserId, entities: List<UserResponse>) {
        userRepository.updateUser(entities.first().toUser())
    }

    override suspend fun onResetAll(userId: UserId) {
        userRepository.getUser(userId, refresh = true)
    }
}
