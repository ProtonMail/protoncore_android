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

package me.proton.core.accountmanager.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.accountmanager.domain.entity.Account
import me.proton.core.accountmanager.domain.entity.AccountState
import me.proton.core.accountmanager.domain.entity.SessionState
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

interface AccountRepository {
    /**
     * Get [Account], by userId.
     */
    fun getAccount(userId: String): Flow<Account>

    /**
     * Get all persisted [Account] on this device.
     */
    fun getAccounts(): Flow<List<Account>>

    /**
     * Get [Account] if exist or null, by userId.
     */
    suspend fun getAccountOrNull(userId: UserId): Account?

    /**
     * Get [Account] if exist or null, by sessionId.
     */
    suspend fun getAccountOrNull(sessionId: SessionId): Account?

    /**
     * Get all persisted [Session] on this device.
     */
    fun getSessions(): Flow<List<Session>>

    /**
     * Get [Session], by sessionId.
     */
    fun getSession(sessionId: SessionId): Flow<Session>

    /**
     * Get [Session], by sessionId.
     */
    fun getSessionOrNull(sessionId: SessionId): Session?

    /**
     * Create or update an [Account], locally.
     */
    suspend fun createOrUpdateAccount(account: Account): Account

    /**
     * Delete an [Account], locally.
     */
    suspend fun deleteAccount(userId: UserId)

    /**
     * Delete an [Session], locally.
     */
    suspend fun deleteSession(sessionId: SessionId)

    /**
     * Update [AccountState], locally.
     */
    suspend fun updateAccountState(userId: UserId, state: AccountState)

    /**
     * Update [SessionState], locally.
     */
    suspend fun updateSessionState(sessionId: SessionId, state: SessionState)

    /**
     * Update session scopes, locally.
     */
    suspend fun updateSessionScopes(sessionId: SessionId, scopes: List<String>)

    /**
     * Update session human verification headers, locally.
     */
    suspend fun updateSessionHeaders(sessionId: SessionId, tokenType: String?, tokenCode: String?)

    /**
     * Update session token, locally.
     */
    suspend fun updateSessionToken(sessionId: SessionId, accessToken: String, refreshToken: String)
}
