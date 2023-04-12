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

package me.proton.core.user.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.user.domain.entity.UserAddress

/**
 * Optional listener to bind the user manager * to key transparency checks.
 * If provided, the listener is called by the user manager
 * when making changes to the signed key list of an address.
 */
interface SignedKeyListChangeListener {

    sealed class Result {
        object Success : Result()
        data class Failure(val reason: Throwable) : Result()
    }


    /**
     * This function is called before uploading the changes to the server.
     * The listener will check the state of KT for the address to make sure
     * that the server is honoring the protocol.
     *
     * @param userId the id of the user
     * @param userAddress the address of the user for which the SKL is
     * being changed.
     */
    suspend fun onSKLChangeRequested(userId: UserId, userAddress: UserAddress): Result

    /**
     * This function is called after uploading the changes to the server.
     * The listener will record the change to local storage to later check
     * that the change was included correctly in the KT log.
     *
     * @param userId the id of the user
     * @param userAddress the address that was changed, refreshed from the server model
     * @param skl the SKL that was uploaded by the client.
     */
    suspend fun onSKLChangeAccepted(userId: UserId, userAddress: UserAddress, skl: PublicSignedKeyList): Result
}
