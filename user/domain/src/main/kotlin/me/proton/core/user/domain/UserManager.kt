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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey

interface UserManager {
    /**
     * Add a [User] and a list of [UserAddress], locally.
     *
     * Note: This function is usually used for importing user/address/key from a different storage or during migration.
     *
     * @throws IllegalStateException if corresponding account doesn't exist.
     * @throws IllegalStateException if corresponding addresses user(s) doesn't exist.
     */
    suspend fun addUser(
        user: User,
        addresses: List<UserAddress>
    )

    /**
     * Get [User], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun getUserFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<User?>>

    /**
     * Get [User], using [sessionUserId].
     *
     * @return value from cache/disk if [refresh] is false, otherwise from fetcher if [refresh] is true.
     */
    suspend fun getUser(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): User

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
     * Try to unlock the primary [UserKey] with the given [password].
     *
     * On [UnlockResult.Success], the passphrase, derived from password, is stored and [UserKey] ready to be used.
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
     * Try to unlock the primary [UserKey] with the given [passphrase].
     *
     * On [UnlockResult.Success], the passphrase is stored and [UserKey] ready to be used.
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
        newPassword: EncryptedString,
        secondFactorCode: String = "",
        proofs: SrpProofs,
        srpSession: String,
        auth: Auth?,
        orgPrivateKey: Armored?
    ): Boolean

    /**
     * Create a new primary [UserKey], [UserAddressKey], and set the derived passphrase for the user.
     */
    suspend fun setupPrimaryKeys(
        sessionUserId: SessionUserId,
        username: String,
        domain: String,
        auth: Auth,
        password: ByteArray
    ): User

    /**
     * Result for [unlockWithPassphrase] and [unlockWithPassword].
     */
    sealed class UnlockResult {
        object Success : UnlockResult()
        sealed class Error : UnlockResult() {
            object NoPrimaryKey : Error()
            object NoKeySaltsForPrimaryKey : Error()
            object PrimaryKeyInvalidPassphrase : Error()
        }
    }
}
