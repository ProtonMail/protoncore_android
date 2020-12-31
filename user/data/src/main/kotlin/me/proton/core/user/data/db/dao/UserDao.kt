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
import me.proton.core.crypto.common.simple.EncryptedByteArray
import me.proton.core.data.db.BaseDao
import me.proton.core.user.data.entity.UserEntity

@Dao
abstract class UserDao : BaseDao<UserEntity>() {

    @Query("SELECT * FROM UserEntity WHERE userId = :userId")
    abstract fun findByUserId(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM UserEntity WHERE userId = :userId")
    abstract suspend fun getByUserId(userId: String): UserEntity?

    @Query("DELETE FROM UserEntity WHERE userId = :userId")
    abstract suspend fun delete(userId: String)

    @Query("DELETE FROM UserEntity")
    abstract suspend fun deleteAll()

    @Transaction
    @Query("SELECT passphrase FROM UserEntity WHERE userId = :userId")
    abstract suspend fun getPassphrase(userId: String): EncryptedByteArray?

    @Transaction
    @Query("UPDATE UserEntity SET passphrase = :passphrase WHERE userId = :userId")
    abstract suspend fun setPassphrase(userId: String, passphrase: EncryptedByteArray?)
}
