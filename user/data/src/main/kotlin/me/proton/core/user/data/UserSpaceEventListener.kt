/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.user.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserSpaceEvent(
    @SerialName("UsedSpace")
    val usedSpace: Long? = null,
)

@Singleton
open class UserSpaceEventListener @Inject constructor(
    private val db: UserDatabase,
    private val userRepository: UserRepository
) : EventListener<String, UserSpaceEvent>() {

    override val type = Type.Core
    override val order = 0

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, UserSpaceEvent>>? {
        return response.body.deserialize<UserSpaceEvent>().let { userSpaceEvent ->
            listOf(Event(Action.Update, config.userId.id, userSpaceEvent))
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.inTransaction(block)

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<UserSpaceEvent>) {
        val userSpace = entities.firstOrNull()
        userSpace?.usedSpace?.let { userRepository.updateUserUsedSpace(config.userId, userSpace.usedSpace) }
    }
}
