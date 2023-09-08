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

import me.proton.core.accountmanager.domain.LogTag.SESSION_CREATE
import me.proton.core.accountmanager.domain.LogTag.SESSION_FORCE_LOGOUT
import me.proton.core.accountmanager.domain.LogTag.SESSION_REFRESH
import me.proton.core.accountmanager.domain.LogTag.SESSION_SCOPES
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.toStringLog
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

open class SessionListenerImpl @Inject constructor(
    private val sessionManager: dagger.Lazy<SessionManager>
) : SessionListener {

    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T {
        return sessionManager.get().withLock(sessionId, action)
    }

    override suspend fun requestSession(): Boolean {
        return sessionManager.get().requestSession()
    }

    override suspend fun refreshSession(session: Session): Boolean {
        return sessionManager.get().refreshSession(session)
    }

    override suspend fun onSessionTokenCreated(userId: UserId?, session: Session) {
        CoreLogger.i(SESSION_CREATE, "Session created: ${session.toStringLog()}")
    }

    override suspend fun onSessionTokenRefreshed(session: Session) {
        CoreLogger.i(SESSION_REFRESH, "Session refreshed: ${session.toStringLog()}")
    }

    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {
        sessionManager.get().getSession(sessionId)?.let {
            CoreLogger.i(SESSION_SCOPES, "Session scopes refreshed: ${it.toStringLog()}")
        }
    }

    override suspend fun onSessionForceLogout(session: Session, httpCode: Int) {
        val isError = when {
            // Unauthenticated Session should not be reported as error.
            session is Session.Unauthenticated -> false
            // Only 400 should be reported as error
            httpCode == HttpResponseCodes.HTTP_BAD_REQUEST -> true
            else -> false
        }
        val message = "Session force logout: ${session.toStringLog()}"
        if (isError) {
            CoreLogger.e(SESSION_FORCE_LOGOUT, message)
        } else {
            CoreLogger.i(SESSION_FORCE_LOGOUT, message)
        }
    }
}
