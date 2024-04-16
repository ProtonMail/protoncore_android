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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.LogTag
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.onError
import me.proton.core.network.domain.onSuccess
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.domain.session.toStringLog
import me.proton.core.util.kotlin.CoreLogger
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SessionManagerImpl @Inject constructor(
    private val sessionListener: SessionListener,
    private val sessionProvider: SessionProvider,
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val accountManager: AccountManager,
    private val monoClock: () -> Long
) : SessionManager, SessionProvider by sessionProvider {

    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T =
        sessionMutex(sessionId).withLock {
            action()
        }

    override suspend fun requestSession(): Boolean =
        sessionMutex(null).withLock {
            internalRequestSession()
        }

    override suspend fun refreshSession(session: Session): Boolean =
        sessionMutex(session.sessionId).withLock {
            internalRefreshSession(session)
        }

    override suspend fun refreshScopes(sessionId: SessionId) =
        sessionMutex(sessionId).withLock {
            internalRefreshScopes(sessionId)
        }

    private suspend fun internalRequestSession(): Boolean {
        // Don't request an Unauthenticated Session if it already exist.
        if (getSessionId(userId = null) != null) return true
        CoreLogger.i(LogTag.SESSION_REQUEST, "Session request...")
        return authRepository.requestSession()
            .onSuccess {
                lastRefreshMap[it.sessionId] = monoClock()
                internalSessionTokenCreated(userId = null, it)
            }.isSuccess
    }

    private suspend fun internalRefreshSession(session: Session): Boolean {
        fun log(message: String) = CoreLogger.i(LogTag.SESSION_REFRESH, message)
        val lastRefresh = lastRefreshMap[session.sessionId] ?: Long.MIN_VALUE
        val refreshedRecently = monoClock() <= lastRefresh + refreshDebounceMs
        val currentSession = sessionProvider.getSession(session.sessionId)
        val sessionChanged = currentSession != session
        // Don't attempt if refreshed recently. Prevent race issues.
        when {
            refreshedRecently -> { log("Session refreshed recently, skipping."); return true }
            sessionChanged -> { log("Session changed: ${session.toStringLog()}"); return true }
        }
        log("Session refreshing: ${session.toStringLog()}")
        return authRepository.refreshSession(session)
            .onSuccess { refreshed ->
                lastRefreshMap[session.sessionId] = monoClock()
                internalSessionTokenRefreshed(refreshed)
            }.onError { error ->
                if (error is ApiResult.Error.Http && error.httpCode in FORCE_LOGOUT_HTTP_CODES) {
                    lastRefreshMap[session.sessionId] = monoClock()
                    internalSessionForceLogout(session, error.httpCode)
                    // Retry the call if it was an unauthenticated session and got new one.
                    return session is Session.Unauthenticated && internalRequestSession()
                }
            }.isSuccess
    }

    private suspend fun internalRefreshScopes(sessionId: SessionId) {
        authRepository.getScopes(sessionId).also { scopes ->
            internalSessionScopesRefreshed(sessionId, scopes)
        }
    }

    private suspend fun internalSessionTokenCreated(userId: UserId?, session: Session) {
        accountRepository.createOrUpdateSession(userId = userId, session = session)
        sessionListener.onSessionTokenCreated(userId, session)
    }

    private suspend fun internalSessionTokenRefreshed(session: Session) {
        accountRepository.updateSessionToken(
            session.sessionId,
            session.accessToken,
            session.refreshToken
        )
        accountRepository.updateSessionState(session.sessionId, SessionState.Authenticated)
        internalSessionScopesRefreshed(session.sessionId, session.scopes)
        sessionListener.onSessionTokenRefreshed(session)
    }

    private suspend fun internalSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {
        accountRepository.updateSessionScopes(sessionId, scopes)
        sessionListener.onSessionScopesRefreshed(sessionId, scopes)
    }

    private suspend fun internalSessionForceLogout(session: Session, httpCode: Int) {
        accountRepository.updateSessionState(session.sessionId, SessionState.ForceLogout)
        accountRepository.getAccountOrNull(session.sessionId)?.let { account ->
            accountManager.disableAccount(account.userId, waitForCompletion = false)
        }
        accountRepository.deleteSession(session.sessionId)
        sessionListener.onSessionForceLogout(session, httpCode)
    }

    companion object {
        val FORCE_LOGOUT_HTTP_CODES = listOf(
            HttpResponseCodes.HTTP_UNAUTHORIZED,
            HttpResponseCodes.HTTP_BAD_REQUEST,
            HttpResponseCodes.HTTP_UNPROCESSABLE
        )

        private val refreshDebounceMs = TimeUnit.MINUTES.toMillis(1)
        private val staticMutex: Mutex = Mutex()
        private val mutexMap: MutableMap<SessionId?, Mutex> = HashMap()
        private var lastRefreshMap: MutableMap<SessionId?, Long> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId?) =
            staticMutex.withLock { mutexMap.getOrPut(sessionId) { Mutex() } }

        @TestOnly
        suspend fun reset(sessionId: SessionId?) =
            sessionMutex(sessionId).withLock { lastRefreshMap[sessionId] = Long.MIN_VALUE }

        @TestOnly
        fun clear() = lastRefreshMap.clear()
    }
}
