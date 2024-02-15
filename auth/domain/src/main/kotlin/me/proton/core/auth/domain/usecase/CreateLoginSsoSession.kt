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

package me.proton.core.auth.domain.usecase

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.network.domain.session.Session
import javax.inject.Inject

class CreateLoginSsoSession @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLoginSso: PerformLoginSso
) {
    suspend operator fun invoke(
        email: String,
        token: String,
        requiredAccountType: AccountType,
    ): SessionInfo {
        val sessionInfo = performLoginSso.invoke(email, token)
        handleSessionInfo(requiredAccountType, sessionInfo)
        return sessionInfo
    }

    private suspend fun handleSessionInfo(
        requiredAccountType: AccountType,
        sessionInfo: SessionInfo,
    ) {
        val account = Account(
            username = sessionInfo.username,
            userId = sessionInfo.userId,
            email = sessionInfo.username.takeIf { it?.contains('@') ?: false },
            sessionId = sessionInfo.sessionId,
            state = AccountState.NotReady,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(
                session = SessionDetails(
                    initialEventId = sessionInfo.eventId,
                    requiredAccountType = requiredAccountType,
                    secondFactorEnabled = sessionInfo.isSecondFactorNeeded,
                    twoPassModeEnabled = sessionInfo.isTwoPassModeNeeded,
                    password = null
                )
            )
        )
        val session = Session.Authenticated(
            userId = sessionInfo.userId,
            sessionId = sessionInfo.sessionId,
            accessToken = sessionInfo.accessToken,
            refreshToken = sessionInfo.refreshToken,
            scopes = sessionInfo.scopes,
        )
        accountWorkflow.handleSession(account, session)
    }
}
