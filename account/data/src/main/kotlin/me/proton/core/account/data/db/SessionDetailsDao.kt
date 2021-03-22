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

package me.proton.core.account.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.data.db.BaseDao
import me.proton.core.network.domain.session.SessionId

@Dao
abstract class SessionDetailsDao : BaseDao<SessionDetailsEntity>() {

    @Query("SELECT * FROM SessionDetailsEntity WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: SessionId): Flow<SessionDetailsEntity?>

    @Query("SELECT * FROM SessionDetailsEntity WHERE sessionId = :sessionId")
    abstract suspend fun getBySessionId(sessionId: SessionId): SessionDetailsEntity?

    @Query("DELETE FROM SessionDetailsEntity WHERE sessionId = :sessionId")
    abstract suspend fun delete(sessionId: SessionId)

    @Query("UPDATE SessionDetailsEntity SET password = null WHERE sessionId = :sessionId")
    abstract suspend fun clearPassword(sessionId: SessionId)
}
