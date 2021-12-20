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
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.LoggerLogTag

object MockSession {
    fun getDefault() = Session(
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
    private val onScopesRefreshed: (sessionId: SessionId, scopes: List<String>) -> Unit = { _, _ -> },
    private val onTokenRefreshed: (Session) -> Unit = { },
    private val onForceLogout: (Session) -> Unit = { },
//    private val onVerificationNeeded: (Session, HumanVerificationApiDetails?) -> SessionListener.HumanVerificationResult = { _, _ ->
//        SessionListener.HumanVerificationResult.Success
//    }
) : SessionListener {
    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) =
        onScopesRefreshed(sessionId, scopes)

    override suspend fun onSessionTokenRefreshed(session: Session) = onTokenRefreshed(session)
    override suspend fun onSessionForceLogout(session: Session) = onForceLogout(session)
//    override suspend fun onHumanVerificationNeeded(
//        session: Session,
//        details: HumanVerificationApiDetails
//    ): SessionListener.HumanVerificationResult = onVerificationNeeded(session, details)
}

class MockApiClient : ApiClient {

    var forceUpdated = false

    override var shouldUseDoh = true
    override val appVersionHeader = "TestApp_1.0"
    override val userAgent = "Test/1.0 (Android 10; brand model)"

    override val enableDebugLogging: Boolean = true

    override var backoffRetryCount = 2

    override fun forceUpdate(errorMessage: String) {
        forceUpdated = true
    }

    override suspend fun missingScope(scope: Scope): MissingScopeResult {
        return MissingScopeResult.Failure
    }
}

class MockNetworkPrefs : NetworkPrefs {
    override var activeAltBaseUrl: String? = null
    override var lastPrimaryApiFail: Long = Long.MIN_VALUE
    override var alternativeBaseUrls: List<String>? = null
}

class MockLogger : Logger {
    override fun e(tag: String, e: Throwable) {
        Log.e(tag, "no message", e)
    }

    override fun e(tag: String, e: Throwable, message: String) {
        Log.e(tag, message, e)
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

    override fun log(tag: LoggerLogTag, message: String) {
        Log.d(tag.name, message)
    }
}
