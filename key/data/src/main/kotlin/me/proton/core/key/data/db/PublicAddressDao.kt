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

package me.proton.core.key.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.key.data.entity.PublicAddressEntity

@Dao
abstract class PublicAddressDao : BaseDao<PublicAddressEntity>() {

    @Query("SELECT * FROM PublicAddressEntity WHERE email = :email")
    abstract fun findByEmail(email: String): Flow<PublicAddressEntity?>

    @Query("DELETE FROM PublicAddressEntity WHERE email = :email")
    abstract suspend fun deleteByEmail(email: String)

    @Query("DELETE FROM PublicAddressEntity")
    abstract suspend fun deleteAll()
}
