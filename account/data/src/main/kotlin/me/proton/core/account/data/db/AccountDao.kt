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
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.data.db.BaseDao

@Dao
abstract class AccountDao : BaseDao<AccountEntity>() {

    @Query("SELECT * FROM AccountEntity")
    abstract fun findAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM AccountEntity WHERE userId = :userId")
    abstract fun findByUserId(userId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM AccountEntity WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: String): Flow<AccountEntity?>

    @Query("UPDATE AccountEntity SET state = :state WHERE userId = :userId")
    abstract suspend fun updateAccountState(userId: String, state: AccountState)

    @Query("UPDATE AccountEntity SET sessionState = :state WHERE sessionId = :sessionId")
    abstract suspend fun updateSessionState(sessionId: String, state: SessionState)

    @Query("UPDATE AccountEntity SET sessionId = :sessionId WHERE userId = :userId")
    abstract suspend fun addSession(userId: String, sessionId: String)

    @Query("UPDATE AccountEntity SET sessionId = null, sessionState = null WHERE sessionId = :sessionId")
    abstract suspend fun removeSession(sessionId: String)

    @Query("DELETE FROM AccountEntity WHERE userId = :userId")
    abstract suspend fun delete(userId: String)

    @Query("DELETE FROM AccountEntity")
    abstract suspend fun deleteAll()
}
