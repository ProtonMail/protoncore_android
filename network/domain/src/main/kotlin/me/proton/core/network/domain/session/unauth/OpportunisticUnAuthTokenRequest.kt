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

package me.proton.core.network.domain.session.unauth

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.UnAuthSessionsRepository
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

class OpportunisticUnAuthTokenRequest @Inject constructor(
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    private val unAuthSessionsRepository: UnAuthSessionsRepository
) {

    suspend operator fun invoke() {
        val sessions = sessionProvider.getSessions()
        if (sessions.isEmpty()) requestSession()
    }

    private suspend fun requestSession(): Boolean {
        return try {
            val result = unAuthSessionsRepository.requestToken()
            sessionListener.onSessionTokenCreated(result) // store it
            true
        } catch (exception: ApiException) {
            CoreLogger.e(LogTag.REQUEST_TOKEN, exception)
            false
        }
    }
}

object LogTag {
    /** Default tag */
    const val REQUEST_TOKEN = "core.network.token.request.opportunistic"
}
