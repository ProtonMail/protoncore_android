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

package me.proton.core.auth.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.data.entity.MemberDeviceEntity
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId

@Dao
abstract class MemberDeviceDao : BaseDao<MemberDeviceEntity>() {
    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId AND deviceId = :deviceId")
    abstract fun getByDeviceId(userId: UserId, deviceId: MemberDeviceId): List<MemberDeviceEntity>

    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId AND memberId = :memberId")
    abstract fun getByMemberId(userId: UserId, memberId: UserId): List<MemberDeviceEntity>

    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId")
    abstract fun getByUserId(userId: UserId): List<MemberDeviceEntity>

    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId AND deviceId = :deviceId")
    abstract fun observeByDeviceId(userId: UserId, deviceId: MemberDeviceId): Flow<List<MemberDeviceEntity>>

    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId AND memberId = :memberId")
    abstract fun observeByMemberId(userId: UserId, memberId: UserId): Flow<List<MemberDeviceEntity>>

    @Query("SELECT * FROM MemberDeviceEntity WHERE userId = :userId")
    abstract fun observeByUserId(userId: UserId): Flow<List<MemberDeviceEntity>>

    @Query("DELETE FROM MemberDeviceEntity")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM MemberDeviceEntity WHERE userId = :userId")
    abstract suspend fun deleteAll(userId: UserId)

    @Transaction
    open suspend fun deleteByMemberId(userId: UserId, memberIds: List<UserId>) {
        memberIds.chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach {
            deleteByMemberIdBatch(userId, it)
        }
    }

    @Transaction
    open suspend fun deleteByDeviceId(userId: UserId, deviceIds: List<MemberDeviceId>) {
        deviceIds.chunked(SQLITE_MAX_VARIABLE_NUMBER).forEach {
            deleteByDeviceIdBatch(userId, it)
        }
    }

    @Query("DELETE FROM MemberDeviceEntity WHERE userId = :userId AND memberId IN (:memberIds)")
    protected abstract suspend fun deleteByMemberIdBatch(userId: UserId, memberIds: List<UserId>)

    @Query("DELETE FROM MemberDeviceEntity WHERE userId = :userId AND deviceId IN (:deviceIds)")
    protected abstract suspend fun deleteByDeviceIdBatch(userId: UserId, deviceIds: List<MemberDeviceId>)
}
