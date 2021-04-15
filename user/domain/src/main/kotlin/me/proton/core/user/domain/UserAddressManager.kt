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

package me.proton.core.user.domain

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey

interface UserAddressManager {
    /**
     * Get all [UserAddress], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun getAddressesFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<List<UserAddress>>>

    /**
     * Get all [UserAddress], using [sessionUserId].
     *
     * @return value from cache/disk if [refresh] is false, otherwise from fetcher if [refresh] is true.
     */
    suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): List<UserAddress>

    /**
     * Get [UserAddress], by [addressId], using [sessionUserId].
     *
     * @return value from cache/disk if [refresh] is false, otherwise from fetcher if [refresh] is true.
     */
    suspend fun getAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean = false
    ): UserAddress?

    /**
     * Create new primary internal [UserAddress], with a new [UserAddressKey], remotely.
     */
    suspend fun setupInternalAddress(
        sessionUserId: SessionUserId,
        username: String,
        domain: String
    ): UserAddress

    /**
     * Update an [UserAddress] with optional [displayName] and optional [signature], remotely.
     */
    suspend fun updateAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        displayName: String? = null,
        signature: String? = null
    ): UserAddress

    /**
     * Update addresses order providing [List] of [AddressId], remotely.
     *
     * Usage example: change the default address.
     */
    suspend fun updateOrder(
        sessionUserId: SessionUserId,
        addressIds: List<AddressId>
    ): List<UserAddress>
}
