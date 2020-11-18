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

package me.proton.core.account.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

interface AccountRepository {
    /**
     * Get [Account], by userId.
     */
    fun getAccount(userId: UserId): Flow<Account?>

    /**
     * Get [Account], by sessionId.
     */
    fun getAccount(sessionId: SessionId): Flow<Account?>

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
    fun getSession(sessionId: SessionId): Flow<Session?>

    /**
     * Get [Session], by sessionId.
     */
    suspend fun getSessionOrNull(sessionId: SessionId): Session?

    /**
     * Get [SessionId], by userId.
     */
    suspend fun getSessionIdOrNull(userId: UserId): SessionId?

    /**
     * Create or update an [Account], locally.
     */
    suspend fun createOrUpdateAccountSession(account: Account, session: Session)

    /**
     * Delete an [Account], locally.
     */
    suspend fun deleteAccount(userId: UserId)

    /**
     * Delete an [Session], locally.
     */
    suspend fun deleteSession(sessionId: SessionId)

    /**
     * Flow of [Account] where [Account.state] changed.
     */
    fun onAccountStateChanged(): Flow<Account>

    /**
     * Flow of [Account] where [Account.sessionState] changed.
     */
    fun onSessionStateChanged(): Flow<Account>

    /**
     * Update [AccountState], by [UserId], locally.
     */
    suspend fun updateAccountState(userId: UserId, state: AccountState)

    /**
     * Update [AccountState], by [SessionId], locally.
     */
    suspend fun updateAccountState(sessionId: SessionId, state: AccountState)

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

    /**
     * Get the primary account [UserId], if exist, null otherwise.
     */
    fun getPrimaryUserId(): Flow<UserId?>

    /**
     * Set the primary [UserId].
     */
    suspend fun setAsPrimary(userId: UserId)

    /**
     * Get [HumanVerificationDetails], if exist, by sessionId.
     */
    suspend fun getHumanVerificationDetails(id: SessionId): HumanVerificationDetails?

    /**
     * Set [HumanVerificationDetails], by sessionId.
     */
    suspend fun setHumanVerificationDetails(id: SessionId, details: HumanVerificationDetails?)
}
