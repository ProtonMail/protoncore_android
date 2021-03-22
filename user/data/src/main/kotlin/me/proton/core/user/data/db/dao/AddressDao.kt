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
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.domain.entity.AddressId

@Dao
abstract class AddressDao : BaseDao<AddressEntity>() {

    @Query("SELECT * FROM AddressEntity WHERE addressId = :addressId")
    abstract fun findByAddressId(addressId: AddressId): Flow<AddressEntity?>

    @Query("SELECT * FROM AddressEntity WHERE userId = :userId")
    abstract fun findAllByUserId(userId: UserId): Flow<List<AddressEntity>>

    @Query("SELECT * FROM AddressEntity WHERE addressId = :addressId")
    abstract suspend fun getByAddressId(addressId: AddressId): AddressEntity?

    @Query("SELECT * FROM AddressEntity WHERE userId = :userId")
    abstract suspend fun getAllUserId(userId: UserId): List<AddressEntity>

    @Query("DELETE FROM AddressEntity WHERE addressId = :addressId")
    abstract suspend fun delete(addressId: AddressId)

    @Query("DELETE FROM AddressEntity WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: UserId)

    @Query("DELETE FROM AddressEntity")
    abstract suspend fun deleteAll()
}
