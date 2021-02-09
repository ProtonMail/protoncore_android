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
import me.proton.core.crypto.common.simple.EncryptedByteArray
import me.proton.core.crypto.common.simple.PlainByteArray
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress

interface UserManager {

    /**
     * Get [User], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun getUser(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<User?>>

    /**
     * Get all [UserAddress], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<List<UserAddress>>>

    /**
     * Try to unlock the user with the given [password].
     *
     * On [UnlockResult.Success], the passphrase, derived from password, is stored and the [User] keys ready to be used.
     *
     * @param userId [UserId] to unlock.
     * @param password [PlainByteArray] to use to unlock.
     * @param refresh if false, use data from cache/disk, otherwise data are refreshed before proceeding.
     *
     * @see [User.keys]
     * @see [areAllLocked]
     */
    suspend fun unlockWithPassword(
        userId: UserId,
        password: PlainByteArray,
        refresh: Boolean = false
    ): UnlockResult

    /**
     * Try to unlock the user with the given [passphrase].
     *
     * On [UnlockResult.Success], the passphrase is stored and the [User] keys ready to be used.
     *
     * @param userId [UserId] to unlock.
     * @param passphrase [EncryptedByteArray] to use to unlock.
     * @param refresh if false, use data from cache/disk, otherwise data is refreshed before proceeding.
     *
     * @see [User.keys]
     * @see [areAllLocked]
     */
    suspend fun unlockWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray,
        refresh: Boolean = false
    ): UnlockResult

    /**
     * Lock the User Keys.
     *
     * The passphrase is cleared, and the User Keys not anymore ready to be used.
     */
    suspend fun lock(userId: UserId)

    /**
     * Change password for a [userId].
     *
     * Note: This function takes care to adapt any needed passphrase or key.
     */
    suspend fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    )
}
