/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.getFido2AuthOptions
import me.proton.core.network.domain.session.Session
import me.proton.core.util.kotlin.serialize
import javax.inject.Inject

/**
 * Create a credentialless Session.
 *
 * Note: Next step should be PostLoginLessAccountSetup.
 */
class CreateLoginLessSession @Inject constructor(
    private val requiredAccountType: AccountType,
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLoginLess: PerformLoginLess
) {
    suspend operator fun invoke(): SessionInfo {
        val sessionInfo = performLoginLess.invoke()
        handleSessionInfo(sessionInfo)
        return sessionInfo
    }

    private suspend fun handleSessionInfo(sessionInfo: SessionInfo) {
        val sessionState = if (sessionInfo.isSecondFactorNeeded) {
            SessionState.SecondFactorNeeded
        } else {
            SessionState.Authenticated
        }

        val account = Account(
            userId = sessionInfo.userId,
            username = null,
            email = null,
            sessionId = sessionInfo.sessionId,
            state = AccountState.NotReady,
            sessionState = sessionState,
            details = AccountDetails(
                session = SessionDetails(
                    initialEventId = sessionInfo.eventId,
                    requiredAccountType = requiredAccountType,
                    secondFactorEnabled = sessionInfo.isSecondFactorNeeded,
                    twoPassModeEnabled = sessionInfo.isTwoPassModeNeeded,
                    password = null,
                    fido2AuthenticationOptionsJson = sessionInfo.getFido2AuthOptions()?.serialize()
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
