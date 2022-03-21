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

package me.proton.core.challenge.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.challenge.data.entity.ChallengeFrameEntity
import me.proton.core.data.room.db.BaseDao

@Dao
abstract class ChallengeFramesDao : BaseDao<ChallengeFrameEntity>() {

    @Query("SELECT * FROM ChallengeFrameEntity")
    abstract fun getAll(): Flow<List<ChallengeFrameEntity>>

    @Query("SELECT * FROM ChallengeFrameEntity WHERE clientId = :clientId")
    abstract suspend fun getByClientId(clientId: String): List<ChallengeFrameEntity>?

    @Query("SELECT * FROM ChallengeFrameEntity WHERE clientId = :clientId AND challengeType = :challengeType")
    abstract suspend fun getByClientIdAndType(clientId: String, challengeType: String): ChallengeFrameEntity?

    @Query("DELETE FROM ChallengeFrameEntity")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM ChallengeFrameEntity WHERE clientId = :clientId")
    abstract suspend fun deleteByClientId(clientId: String)

    @Query("UPDATE ChallengeFrameEntity SET focusTime = :focusTime, clicks = :clicks, copy = :copy, paste = :paste WHERE clientId = :clientId")
    abstract suspend fun updateFrame(
        clientId: String,
        focusTime: Long,
        clicks: Int,
        copy: List<String>,
        paste: List<String>
    )
}
