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
import me.proton.core.user.data.entity.AddressEntity

@Dao
abstract class AddressDao : BaseDao<AddressEntity>() {

    @Query("SELECT * FROM AddressEntity WHERE addressId = :addressId")
    abstract fun findByAddressId(addressId: String): Flow<AddressEntity?>

    @Query("SELECT * FROM AddressEntity WHERE userId = :userId")
    abstract fun findAllByUserId(userId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM AddressEntity WHERE addressId = :addressId")
    abstract suspend fun getByAddressId(addressId: String): AddressEntity?

    @Query("SELECT * FROM AddressEntity WHERE userId = :userId")
    abstract suspend fun getAllUserId(userId: String): List<AddressEntity>

    @Query("DELETE FROM AddressEntity WHERE addressId = :addressId")
    abstract suspend fun delete(addressId: String)

    @Query("DELETE FROM AddressEntity")
    abstract suspend fun deleteAll()
}
