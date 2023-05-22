package me.proton.core.accountmanager.data

import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.network.domain.session.SessionId

class TestSessionListener(
    sessionManager: dagger.Lazy<SessionManager>
) : SessionListenerImpl(sessionManager) {

    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T {
        return action()
    }
}
