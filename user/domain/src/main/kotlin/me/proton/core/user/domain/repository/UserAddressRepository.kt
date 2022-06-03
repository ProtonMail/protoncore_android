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

package me.proton.core.user.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress

interface UserAddressRepository {
    /**
     * Add [UserAddress], locally.
     *
     * Note: This function is usually used for importing address/key from a different storage or during migration.
     *
     * @throws IllegalStateException if corresponding user(s) doesn't exist.
     */
    suspend fun addAddresses(
        addresses: List<UserAddress>
    )

    /**
     * Update [UserAddress], locally.
     *
     * Note: This function is usually used for Events handling.
     *
     * @throws IllegalStateException if corresponding user(s) doesn't exist.
     */
    suspend fun updateAddresses(
        addresses: List<UserAddress>
    )

    /**
     * Delete [UserAddress], locally.
     *
     * Note: This function is usually used for Events handling.
     */
    suspend fun deleteAddresses(
        addressIds: List<AddressId>
    )

    /**
     * Delete all [UserAddress] for a given [userId], locally.
     *
     * Note: This function is usually used for Events handling.
     */
    suspend fun deleteAllAddresses(
        userId: UserId
    )

    /**
     * Observe all [UserAddress], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<List<UserAddress>>

    /**
     * Observe [UserAddress], by [addressId], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun observeAddress(
        sessionUserId: SessionUserId,
        addressId: AddressId,
        refresh: Boolean = false
    ): Flow<UserAddress?>

    /**
     * Get all [UserAddress], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    @Deprecated(
        "Use observeAddresses() instead, DataResult is not needed for this object.",
        ReplaceWith("observeAddresses(sessionUserId, refresh)")
    )
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
     * Create new [UserAddress] with [displayName] and [domain], remotely.
     */
    suspend fun createAddress(
        sessionUserId: SessionUserId,
        displayName: String,
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
