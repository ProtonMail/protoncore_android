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

package me.proton.core.userrecovery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.userrecovery.data.entity.RecoveryFileEntity

@Dao
abstract class DeviceRecoveryDao : BaseDao<RecoveryFileEntity>() {
    @Query("DELETE FROM RecoveryFileEntity WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: UserId)

    @Query("SELECT * FROM RecoveryFileEntity WHERE userId = :userId")
    abstract suspend fun getRecoveryFiles(userId: UserId): List<RecoveryFileEntity>
}
