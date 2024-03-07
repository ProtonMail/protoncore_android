/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.user.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User

interface UserLocalDataSource {
    suspend fun getPassphrase(userId: UserId): EncryptedByteArray?
    suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray?, onSuccess: suspend () -> Unit)

    suspend fun getUser(userId: UserId): User?
    fun observe(userId: UserId): Flow<User?>
    suspend fun upsert(user: User, onSuccess: (suspend () -> Unit)? = null)

    suspend fun updateUserUsedSpace(userId: UserId, usedSpace: Long)
    suspend fun updateUserUsedBaseSpace(userId: UserId, usedBaseSpace: Long)
    suspend fun updateUserUsedDriveSpace(userId: UserId, usedDriveSpace: Long)
}
