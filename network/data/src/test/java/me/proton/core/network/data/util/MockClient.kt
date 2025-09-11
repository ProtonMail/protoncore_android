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
package me.proton.core.network.data.util

import android.util.Log
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.util.kotlin.Logger

object MockSession {
    fun getDefault() = Session.Authenticated(
        userId = UserId("userId"),
        sessionId = SessionId("uid"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("mail", "vpn", "calendar")
    )
}

object MockClientId {
    fun getForSession(sessionId: SessionId) = ClientId.AccountSession(sessionId)
    fun getForCookie(cookieSessionId: CookieSessionId) = ClientId.CookieSession(cookieSessionId)
}

class MockSessionListener(
    private val request: () -> Boolean = { true },
    private val refresh: (Session) -> Boolean = { true },
    private val onScopesRefreshed: (sessionId: SessionId, scopes: List<String>) -> Unit = { _, _ -> },
    private val onTokenCreated: (Session) -> Unit = { },
    private val onTokenRefreshed: (Session) -> Unit = { },
    private val onForceLogout: (Session, Int) -> Unit = { _, _ -> }
) : SessionListener {
    override suspend fun onSessionScopesRefreshed(
        sessionId: SessionId,
        scopes: List<String>
    ) = onScopesRefreshed(sessionId, scopes)

    override suspend fun onSessionTokenCreated(
        userId: UserId?,
        session: Session
    ) = onTokenCreated(session)

    override suspend fun onSessionTokenRefreshed(
        session: Session
    ) = onTokenRefreshed(session)

    override suspend fun onSessionForceLogout(
        session: Session,
        httpCode: Int
    ) = onForceLogout(session, httpCode)

    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T = action()
    override suspend fun requestSession(): Boolean = request()
    override suspend fun refreshSession(session: Session): Boolean = refresh(session)
}

class MockApiClient : ApiClient {

    var forceUpdated = false
    var shouldUseDoh = true

    override val appVersionHeader = "android-mail@1.2.3"
    override val userAgent = "Test/1.0 (Android 10; brand model)"

    override val enableDebugLogging: Boolean = true

    override var backoffRetryCount = 2

    override fun forceUpdate(errorMessage: String) {
        forceUpdated = true
    }

    override suspend fun shouldUseDoh(): Boolean = shouldUseDoh
}

class MockNetworkPrefs : NetworkPrefs {
    override var activeAltBaseUrl: String? = null
    override var lastPrimaryApiFail: Long = Long.MIN_VALUE
    override var alternativeBaseUrls: List<String>? = null
    override var successfulSecondaryDohServiceUrl: String? = null
}

class MockLogger : Logger {
    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun e(tag: String, e: Throwable) {
        Log.e(tag, "no message", e)
    }

    override fun e(tag: String, e: Throwable, message: String) {
        Log.e(tag, message, e)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun w(tag: String, e: Throwable) {
        Log.w(tag, e)
    }

    override fun w(tag: String, e: Throwable, message: String) {
        Log.w(tag, message, e)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun i(tag: String, e: Throwable, message: String) {
        Log.i(tag, message, e)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun d(tag: String, e: Throwable, message: String) {
        Log.d(tag, message, e)
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    override fun v(tag: String, e: Throwable, message: String) {
        Log.v(tag, message, e)
    }
}
