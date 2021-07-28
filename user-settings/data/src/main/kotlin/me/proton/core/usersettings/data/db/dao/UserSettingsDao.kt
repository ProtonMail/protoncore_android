/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.data.entity.UserSettingsEntity

@Dao
abstract class UserSettingsDao : BaseDao<UserSettingsEntity>() {

    @Query("SELECT * FROM UserSettingsEntity WHERE userId = :userId")
    abstract fun observeByUserId(userId: UserId): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM UserSettingsEntity WHERE userId = :userId")
    abstract suspend fun getByUserId(userId: UserId): UserSettingsEntity?

    @Query("DELETE FROM UserSettingsEntity WHERE userId = :userId")
    abstract suspend fun delete(userId: UserId)

    @Query("DELETE FROM UserSettingsEntity")
    abstract suspend fun deleteAll()
}