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
import me.proton.core.key.data.api.response.AddressResponse
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.extension.toAddress
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.util.kotlin.deserializeOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserAddressEvents(
    @SerialName("Addresses")
    val addresses: List<UserAddressEvent>
)

@Serializable
data class UserAddressEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("Address")
    val address: AddressResponse
)

@Singleton
class UserAddressEventListener @Inject constructor(
    private val db: AddressDatabase,
    private val userAddressRepository: UserAddressRepository
) : EventListener<String, AddressResponse>() {

    override val type = Type.Core
    override val order = 1

    override suspend fun deserializeEvents(response: EventsResponse): List<Event<String, AddressResponse>>? {
        return response.body.deserializeOrNull<UserAddressEvents>()?.addresses?.map {
            Event(requireNotNull(Action.map[it.action]), it.address.id, it.address)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R {
        return db.inTransaction(block)
    }

    override suspend fun onCreate(userId: UserId, entities: List<AddressResponse>) {
        userAddressRepository.updateAddresses(entities.map { it.toAddress(userId) })
    }

    override suspend fun onUpdate(userId: UserId, entities: List<AddressResponse>) {
        userAddressRepository.updateAddresses(entities.map { it.toAddress(userId) })
    }

    override suspend fun onDelete(userId: UserId, keys: List<String>) {
        userAddressRepository.deleteAddresses(keys.map { AddressId(it) })
    }

    override suspend fun onResetAll(userId: UserId) {
        userAddressRepository.deleteAllAddresses(userId)
        userAddressRepository.getAddresses(userId, refresh = true)
    }
}
