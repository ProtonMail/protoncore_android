/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.data.local

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.data.local.entity.AddressChangeEntity

@Dao
public abstract class AddressChangeDao : BaseDao<AddressChangeEntity>() {

    @Query("SELECT * FROM AddressChangeEntity WHERE userId = :userId")
    public abstract suspend fun getAddressChanges(userId: UserId): List<AddressChangeEntity>

    @Query(
        """DELETE FROM AddressChangeEntity 
        WHERE userId = :userId AND changeId = :changeId"""
    )
    public abstract suspend fun deleteAddressChange(userId: UserId, changeId: String)
}
