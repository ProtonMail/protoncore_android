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

package me.proton.core.push.data.local.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.PushId

@Dao
public abstract class PushDao : BaseDao<PushEntity>() {

    @Query("SELECT * FROM PushEntity WHERE userId = :userId AND type = :type")
    public abstract fun observeAllPushes(userId: UserId, type: String): Flow<List<PushEntity>>

    @Query("SELECT * FROM PushEntity WHERE userId = :userId AND type = :type")
    public abstract suspend fun getAllPushes(userId: UserId, type: String): List<PushEntity>

    @Query("SELECT * FROM PushEntity WHERE userId = :userId AND pushId = :pushId")
    public abstract suspend fun getPush(userId: UserId, pushId: PushId): PushEntity?

    @Query("DELETE FROM PushEntity")
    public abstract suspend fun deleteAllPushes()

    @Query("DELETE FROM PushEntity WHERE userId IN (:userIds)")
    public abstract suspend fun deletePushes(vararg userIds: UserId)

    @Query("DELETE FROM PushEntity WHERE userId = :userId AND pushId IN (:pushIds)")
    public abstract suspend fun deletePushes(userId: UserId, vararg pushIds: PushId)

    @Query("DELETE FROM PushEntity WHERE userId = :userId AND type = :pushType")
    public abstract suspend fun deletePushes(userId: UserId, pushType: String)
}
