/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.user.data

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener

class TestSessionListener : SessionListener {
    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T {
        return action()
    }

    override suspend fun requestSession(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refreshSession(session: Session): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun onSessionTokenCreated(userId: UserId?, session: Session) {
        TODO("Not yet implemented")
    }

    override suspend fun onSessionTokenRefreshed(session: Session) {
        TODO("Not yet implemented")
    }

    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun onSessionForceLogout(session: Session, httpCode: Int) {
        TODO("Not yet implemented")
    }
}
