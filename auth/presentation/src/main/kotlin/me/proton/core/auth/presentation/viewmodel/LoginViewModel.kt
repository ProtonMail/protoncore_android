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
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.usecase.GetUser
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.auth.domain.usecase.onError
import me.proton.core.auth.domain.usecase.onLoginSuccess
import me.proton.core.auth.domain.usecase.onProcessing
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class serving the Login activity.
 */
class LoginViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin,
    private val performUserSetup: PerformUserSetup,
    private val getUser: GetUser,
    private val updateUsernameOnlyAccount: UpdateUsernameOnlyAccount
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
        password: ByteArray,
        requiredAccountType: AccountType
    ) {
        performLogin(username, password)
            .onProcessing { loginState.post(it) }
            .onLoginSuccess {
                // Storing the session is mandatory for executing subsequent requests.
                handleSessionInfo(it.sessionInfo)
                // Because of API bug, we need to verify if the PasswordMode 2 is really needed (user keys not empty).
                // Also, we can not execute user api call if the account has 2FA, so this has to be done there as well.
                if (!it.sessionInfo.isSecondFactorNeeded) {
                    // But, we can not execute user request if second factor is needed (no sufficient scope).
                    getUser(SessionId(it.sessionInfo.sessionId))
                        .onSuccess { success ->
                            onUserDetails(password, it.sessionInfo, success.user, requiredAccountType)
                        }
                        .onError { error -> loginState.post(PerformLogin.State.Error.FetchUser(error)) }
                        .launchIn(viewModelScope)
                } else {
                    // Raise Success.Login.
                    loginState.post(PerformLogin.State.Success.Login(it.sessionInfo))
                }
            }
            .onError { loginState.post(it) }
            .launchIn(viewModelScope)
    }

    /**
     * Execute a routine when user details result is back from the API.
     */
    private suspend fun onUserDetails(
        password: ByteArray,
        sessionInfo: SessionInfo,
        user: User,
        requiredAccountType: AccountType
    ) {
        // identifies if the account is really two pass mode account (api bug)
        val session = if (sessionInfo.isTwoPassModeNeeded && user.keys.isEmpty()) {
            sessionInfo.copy(passwordMode = 1)
        } else {
            sessionInfo
        }
        if (!session.isTwoPassModeNeeded && user.keys.isNotEmpty()) {
            // If Password mode is 1 pass, we directly setup the user (aka Mailbox Login)
            setupUser(password, session)
        } else {
            // if there are no Address Keys and the current AccountType (Username) does not meet the required.
            if (user.keys.isEmpty() && !user.addresses.satisfiesAccountType(requiredAccountType)) {
                if (user.role == 1 && user.private) {
                    // in this case we just show a dialog for password chooser
                    accountWorkflow.handleAccountNotReady(UserId(sessionInfo.userId))
                    loginState.post(PerformLogin.State.Error.PasswordChange)
                } else {
                    // we upgrade it
                    upgradeUsernameOnlyAccount(
                        username = checkNotNull(user.name) { "For account type `Username`, name should always be present." },
                        password = password,
                        sessionInfo = session
                    )
                }
            } else {
                // otherwise we raise Success.Login if there are Address Keys present.
                loginState.post(
                    PerformLogin.State.Success.UserSetup(
                        sessionInfo = session,
                        user = user.copy(passphrase = password)
                    )
                )
            }
        }
    }

    private fun upgradeUsernameOnlyAccount(
        username: String,
        password: ByteArray,
        domain: String? = null,
        sessionInfo: SessionInfo
    ) {
        updateUsernameOnlyAccount(
            sessionId = SessionId(sessionInfo.sessionId),
            username = username,
            passphrase = password,
            domain = domain
        )
            .onSuccess {
                accountWorkflow.handleAccountReady(UserId(sessionInfo.userId))
                setupUser(password, sessionInfo)
            }
            .onError {
                accountWorkflow.handleAccountNotReady(UserId(sessionInfo.userId))
                loginState.post(PerformLogin.State.Error.AccountUpgrade(it))
            }
            .launchIn(viewModelScope)
    }

    private fun setupUser(password: ByteArray, sessionInfo: SessionInfo) {
        performUserSetup(SessionId(sessionInfo.sessionId), password)
            .onSuccess {
                accountWorkflow.handleAccountReady(UserId(sessionInfo.userId))
                loginState.post(PerformLogin.State.Success.UserSetup(sessionInfo, it.user))
            }
            .onError {
                accountWorkflow.handleAccountNotReady(UserId(sessionInfo.userId))
                loginState.post(PerformLogin.State.Error.UserSetup(it))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun handleSessionInfo(sessionInfo: SessionInfo) {
        val sessionState =
            if (sessionInfo.isSecondFactorNeeded) SessionState.SecondFactorNeeded else SessionState.Authenticated
        val accountState =
            if (sessionInfo.isTwoPassModeNeeded) AccountState.TwoPassModeNeeded else AccountState.NotReady

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
