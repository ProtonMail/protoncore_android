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

package me.proton.core.accountmanager.data

import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import javax.inject.Inject

class SessionListenerImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : SessionListener {

    override suspend fun onSessionTokenCreated(session: Session) {
        accountRepository.createOrUpdateSession(userId = null, session = session)
    }

    override suspend fun onSessionTokenRefreshed(session: Session) {
        accountRepository.updateSessionToken(session.sessionId, session.accessToken, session.refreshToken)
        accountRepository.updateSessionState(session.sessionId, SessionState.Authenticated)
        accountRepository.updateSessionScopes(session.sessionId, session.scopes)
    }

    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {
        accountRepository.updateSessionScopes(sessionId, scopes)
    }

    override suspend fun onSessionForceLogout(session: Session) {
        accountRepository.updateSessionState(session.sessionId, SessionState.ForceLogout)
        accountRepository.getAccountOrNull(session.sessionId)?.let { account ->
            accountRepository.updateAccountState(account.userId, AccountState.Disabled)
        }
        accountRepository.deleteSession(session.sessionId)
    }
}
