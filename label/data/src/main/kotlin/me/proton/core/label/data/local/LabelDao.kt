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

package me.proton.core.label.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class LabelDao : BaseDao<LabelEntity>() {

    @Query("SELECT * FROM LabelEntity WHERE userId = :userId AND type = :type")
    abstract fun observeAll(userId: UserId, type: Int): Flow<List<LabelEntity>>

    @Query("SELECT * FROM LabelEntity WHERE userId = :userId AND type = :type")
    abstract suspend fun getAll(userId: UserId, type: Int): List<LabelEntity>

    @Query("DELETE FROM LabelEntity WHERE userId = :userId AND labelId IN (:labelIds)")
    abstract suspend fun delete(userId: UserId, labelIds: List<String>)

    @Query("DELETE FROM LabelEntity WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: UserId)
}
