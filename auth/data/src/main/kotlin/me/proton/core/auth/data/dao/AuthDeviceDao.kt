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

package me.proton.core.auth.data.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.data.entity.AuthDeviceEntity
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

@Dao
abstract class AuthDeviceDao : BaseDao<AuthDeviceEntity>() {

    @Query("SELECT * FROM AuthDeviceEntity WHERE userId = :userId")
    abstract fun observeByUserId(userId: UserId): Flow<List<AuthDeviceEntity>>

    @Query("SELECT * FROM AuthDeviceEntity WHERE userId = :userId")
    abstract suspend fun getByUserId(userId: UserId): List<AuthDeviceEntity>

    @Query("SELECT * FROM AuthDeviceEntity WHERE addressId = :addressId")
    abstract suspend fun getByAddressId(addressId: AddressId): List<AuthDeviceEntity>

    @Query("DELETE FROM AuthDeviceEntity WHERE userId IN (:userIds)")
    abstract suspend fun deleteAll(vararg userIds: UserId)

    @Query("DELETE FROM AuthDeviceEntity WHERE deviceId IN (:deviceId)")
    abstract suspend fun deleteByDeviceId(vararg deviceId: AuthDeviceId)

    @Query("DELETE FROM AuthDeviceEntity")
    abstract suspend fun deleteAll()
}
