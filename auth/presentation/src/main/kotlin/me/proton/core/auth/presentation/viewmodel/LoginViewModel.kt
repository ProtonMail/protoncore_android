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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.Account
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.DispatcherProvider
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class serving the Login activity.
 */
class LoginViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin
) : ProtonViewModel(), ViewStateStoreScope {

    val loginState = ViewStateStore<PerformLogin.LoginState>().lock

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
            .onEach {
                if (it is PerformLogin.LoginState.Success) {
                    // on success result, contact account manager
                    onSuccess(it)
                }
                // inform the view for each state change
                loginState.post(it)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun onSuccess(success: PerformLogin.LoginState.Success) {
        val result = success.sessionInfo
        val account = Account(
            username = result.username,
            userId = UserId(result.userId),
            email = null,
            sessionId = SessionId(result.sessionId),
            isMailboxLoginNeeded = result.isMailboxLoginNeeded,
            isSecondFactorNeeded = result.isSecondFactorNeeded
        )
        val session = Session(
            sessionId = SessionId(result.sessionId),
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            scopes = result.scopes
        )
        accountWorkflow.handleSession(account, session)
    }
}
