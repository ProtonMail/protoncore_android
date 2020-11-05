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

package me.proton.core.auth.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.onError
import me.proton.core.auth.domain.usecase.onLoginSuccess
import me.proton.core.auth.domain.usecase.onProcessing
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class serving the Login activity.
 */
class LoginViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin,
    private val performUserSetup: PerformUserSetup
) : ProtonViewModel(), ViewStateStoreScope {

    val loginState = ViewStateStore<PerformLogin.State>().lock

    /**
     * Attempts to make the login call.
     *
     * @param username the account's username entered as input
     * @param password the accounts's password entered as input
     */
    fun startLoginWorkflow(
        username: String,
        password: ByteArray
    ) {
        performLogin(username, password)
            .onProcessing { loginState.post(it) }
            .onLoginSuccess {
                handleSessionInfo(it.sessionInfo)
                // No more steps -> directly setup user.
                if (!it.sessionInfo.isTwoPassModeNeeded && !it.sessionInfo.isSecondFactorNeeded) {
                    // Raise Success.UserSetup.
                    setupUser(password, it.sessionInfo)
                } else {
                    // Raise Success.Login.
                    loginState.post(PerformLogin.State.Success.Login(it.sessionInfo))
                }
            }
            .onError { loginState.post(it) }
            .launchIn(viewModelScope)
    }

    private fun setupUser(password: ByteArray, sessionInfo: SessionInfo) {
        performUserSetup(SessionId(sessionInfo.sessionId), password)
            .onSuccess { loginState.post(PerformLogin.State.Success.UserSetup(sessionInfo, it.user)) }
            .onError { loginState.post(PerformLogin.State.Error.UserSetup(it)) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleSessionInfo(sessionInfo: SessionInfo) {
        val sessionState =
            if (sessionInfo.isSecondFactorNeeded) SessionState.SecondFactorNeeded else SessionState.Authenticated

        val accountState =
            if (sessionInfo.isTwoPassModeNeeded) AccountState.TwoPassModeNeeded else AccountState.Ready

        val account = Account(
            username = sessionInfo.username,
            userId = UserId(sessionInfo.userId),
            email = null,
            sessionId = SessionId(sessionInfo.sessionId),
            state = accountState,
            sessionState = sessionState
        )
        val session = Session(
            sessionId = SessionId(sessionInfo.sessionId),
            accessToken = sessionInfo.accessToken,
            refreshToken = sessionInfo.refreshToken,
            scopes = sessionInfo.scopes
        )
        accountWorkflow.handleSession(account, session)
    }
}
