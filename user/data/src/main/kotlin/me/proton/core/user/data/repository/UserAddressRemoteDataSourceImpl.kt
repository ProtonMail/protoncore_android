/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.user.data.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.AddressApi
import me.proton.core.user.data.api.request.CreateAddressRequest
import me.proton.core.user.data.api.request.UpdateAddressRequest
import me.proton.core.user.data.api.request.UpdateOrderRequest
import me.proton.core.user.data.extension.toAddress
import me.proton.core.user.data.extension.toEntity
import me.proton.core.user.data.extension.toEntityList
import me.proton.core.user.data.extension.toUserAddress
import me.proton.core.user.data.extension.toUserAddressKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.isCredentialLess
import me.proton.core.user.domain.repository.UserAddressRemoteDataSource
import me.proton.core.user.domain.repository.UserLocalDataSource
import javax.inject.Inject

class UserAddressRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val userLocalDataSource: UserLocalDataSource
) : UserAddressRemoteDataSource {

    private suspend fun fetchRemote(
        userId: UserId
    ): List<UserAddress> = apiProvider.get<AddressApi>(userId).invoke {
        getAddresses().addresses.map { it.toAddress(userId) }
    }.valueOrThrow

    override suspend fun fetchAll(
        userId: UserId
    ): List<UserAddress> {
        val user = userLocalDataSource.getUser(userId)
        return when {
            user == null -> emptyList()
            user.isCredentialLess() -> emptyList()
            else -> fetchRemote(userId)
        }
    }

    override suspend fun createAddress(
        sessionUserId: SessionUserId,
        displayName: String,
        domain: String
    ): UserAddress = apiProvider.get<AddressApi>(sessionUserId).invoke {
        val response =
            createAddress(CreateAddressRequest(displayName = displayName, domain = domain))
        val address = response.address.toEntity(sessionUserId)
        val addressId = address.addressId
        val addressKeys = response.address.keys?.toEntityList(addressId).orEmpty()
        address.toUserAddress(addressKeys.map { it.toUserAddressKey() })
    }.valueOrThrow

    override suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String?,
        signature: String?
    ) = apiProvider.get<AddressApi>(sessionUserId).invoke {
        updateAddress(
            id = addressId.id,
            request = UpdateAddressRequest(displayName = displayName, signature = signature)
        )
    }.valueOrThrow

    override suspend fun updateOrder(
        sessionUserId: SessionUserId,
        addressIds: List<AddressId>
    ) = apiProvider.get<AddressApi>(sessionUserId).invoke {
        updateOrder(UpdateOrderRequest(ids = addressIds.map { it.id }))
    }.valueOrThrow
}
