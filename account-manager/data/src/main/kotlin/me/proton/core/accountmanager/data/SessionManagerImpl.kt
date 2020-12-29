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

package me.proton.core.accountmanager.data

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener

class SessionManagerImpl(
    private val accountRepository: AccountRepository
) : SessionManager {

    // region SessionListener

    override suspend fun onSessionTokenRefreshed(session: Session) {
        accountRepository.updateSessionToken(session.sessionId, session.accessToken, session.refreshToken)
        accountRepository.updateSessionState(session.sessionId, SessionState.Authenticated)
    }

    override suspend fun onSessionForceLogout(session: Session) {
        accountRepository.updateSessionState(session.sessionId, SessionState.ForceLogout)
        accountRepository.getAccountOrNull(session.sessionId)?.let { account ->
            accountRepository.updateAccountState(account.userId, AccountState.Disabled)
        }
    }

    override suspend fun onHumanVerificationNeeded(
        session: Session,
        details: HumanVerificationDetails
    ): SessionListener.HumanVerificationResult {
        accountRepository.setHumanVerificationDetails(session.sessionId, details)
        accountRepository.updateSessionState(session.sessionId, SessionState.HumanVerificationNeeded)

        // Wait for HumanVerification Success or Failure.
        val state = accountRepository.getAccount(session.sessionId)
            .map { it?.sessionState }
            .filter { it == SessionState.HumanVerificationSuccess || it == SessionState.HumanVerificationFailed }
            .first()

        return when (state) {
            null -> SessionListener.HumanVerificationResult.Failure
            SessionState.HumanVerificationSuccess -> SessionListener.HumanVerificationResult.Success
            else -> SessionListener.HumanVerificationResult.Failure
        }
    }

    // endregion

    // region SessionProvider

    override suspend fun getSession(sessionId: SessionId): Session? =
        accountRepository.getSessionOrNull(sessionId)

    override suspend fun getSessionId(userId: UserId): SessionId? =
        accountRepository.getSessionIdOrNull(userId)

    // endregion
}
