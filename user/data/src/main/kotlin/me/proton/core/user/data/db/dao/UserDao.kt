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

package me.proton.core.user.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

@Dao
abstract class UserDao : BaseDao<UserEntity>() {

    @Query("SELECT * FROM UserEntity WHERE userId = :userId")
    abstract fun observeByUserId(userId: UserId): Flow<UserEntity?>

    @Query("SELECT * FROM UserEntity WHERE userId = :userId")
    abstract suspend fun getByUserId(userId: UserId): UserEntity?

    @Transaction
    @Query("SELECT passphrase FROM UserEntity WHERE userId = :userId")
    abstract suspend fun getPassphrase(userId: UserId): EncryptedByteArray?

    @Transaction
    @Query("UPDATE UserEntity SET passphrase = :passphrase WHERE userId = :userId")
    abstract suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray?)

    @Query("UPDATE UserEntity SET usedSpace = :usedSpace WHERE userId = :userId")
    abstract suspend fun setUsedSpace(userId: UserId, usedSpace: Long)
}
