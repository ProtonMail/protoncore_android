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
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.User

@Suppress("TooManyFunctions")
interface UserRepository : PassphraseRepository {

    /**
     * Add a [User], locally.
     *
     * Note: This function is usually used for importing user/key from a different storage or during migration.
     *
     * @throws IllegalStateException if corresponding account doesn't exist.
     */
    suspend fun addUser(
        user: User
    )

    /**
     * Update a [User], locally.
     *
     * Note: This function is usually used for Events handling.
     *
     * @throws IllegalStateException if corresponding account doesn't exist.
     */
    suspend fun updateUser(
        user: User
    )

    /**
     * Update a [User.usedSpace] for a [userId], locally.
     */
    suspend fun updateUserUsedSpace(
        userId: UserId,
        usedSpace: Long,
    )

    /**
     * Update a [User.usedBaseSpace] for a [userId], locally.
     */
    suspend fun updateUserUsedBaseSpace(
        userId: UserId,
        usedBaseSpace: Long,
    )

    /**
     * Update a [User.usedDriveSpace] for a [userId], locally.
     */
    suspend fun updateUserUsedDriveSpace(
        userId: UserId,
        usedDriveSpace: Long,
    )

    /**
     * Observe [User], using [sessionUserId].
     *
     * @return value emitted from cache/disk, then from fetcher if [refresh] is true.
     */
    fun observeUser(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<User?>

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
     * Create new [User], remotely. Used during signup.
     * @param sessionUserId Must be non-null, if creating an account from credential-less account;
     *  otherwise should be null.
     */
    suspend fun createUser(
        username: String,
        domain: Domain?,
        password: EncryptedString,
        recoveryEmail: String?,
        recoveryPhone: String?,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId? = null
    ): User

    /**
     * Create new [User], remotely. Used during signup.
     * @param sessionUserId Must be non-null, if creating an account from credential-less account;
     *  otherwise should be null.
     */
    suspend fun createExternalEmailUser(
        email: String,
        password: EncryptedString,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId? = null
    ): User

    /**
     * Removes both security scopes [LOCKED, PASSWORD] for the user.
     */
    suspend fun removeLockedAndPasswordScopes(sessionUserId: SessionUserId): Boolean

    /**
     * Adds unlock security scope for the user.
     */
    suspend fun unlockUserForLockedScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String
    ): Boolean

    /**
     * Adds unlock security scope for the user.
     */
    suspend fun unlockUserForPasswordScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        twoFactorCode: String?
    ): Boolean

    /**
     * Check username availability, remotely.
     *
     * @throws ApiException if corresponding username is not available.
     */
    suspend fun checkUsernameAvailable(
        sessionUserId: SessionUserId?,
        username: String
    )

    /**
     * Check external email availability, remotely.
     *
     * @throws ApiException if corresponding external email is not available.
     */
    suspend fun checkExternalEmailAvailable(email: String)
}
