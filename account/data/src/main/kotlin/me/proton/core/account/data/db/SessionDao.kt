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
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.data.db.BaseDao
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

@Dao
abstract class SessionDao : BaseDao<SessionEntity>() {

    @Query("SELECT * FROM SessionEntity WHERE product = :product")
    abstract fun findAll(product: Product): Flow<List<SessionEntity>>

    @Query("SELECT * FROM SessionEntity WHERE sessionId = :sessionId")
    abstract fun findBySessionId(sessionId: SessionId): Flow<SessionEntity?>

    @Query("SELECT * FROM SessionEntity WHERE sessionId = :sessionId")
    abstract suspend fun get(sessionId: SessionId): SessionEntity?

    @Query("SELECT sessionId FROM SessionEntity WHERE userId = :userId")
    abstract suspend fun getSessionId(userId: UserId): SessionId?

    @Query("DELETE FROM SessionEntity WHERE sessionId = :sessionId")
    abstract suspend fun delete(sessionId: SessionId)

    @Query("UPDATE SessionEntity SET scopes = :scopes WHERE sessionId = :sessionId")
    abstract suspend fun updateScopes(sessionId: SessionId, scopes: String)

    @Query("UPDATE SessionEntity SET accessToken = :accessToken, refreshToken = :refreshToken WHERE sessionId = :sessionId")
    abstract suspend fun updateToken(sessionId: SessionId, accessToken: EncryptedString, refreshToken: EncryptedString)
}
