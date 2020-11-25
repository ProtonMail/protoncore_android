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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.usecase.GetUser
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.auth.domain.usecase.onError
import me.proton.core.auth.domain.usecase.onProcessing
import me.proton.core.auth.domain.usecase.onSecondFactorSuccess
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View Model that serves the Second Factor authentication.
 */
class SecondFactorViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor,
    private val performUserSetup: PerformUserSetup,
    private val updateUsernameOnlyAccount: UpdateUsernameOnlyAccount,
    private val getUser: GetUser,
) : ProtonViewModel(), ViewStateStoreScope {

    val secondFactorState = ViewStateStore<PerformSecondFactor.State>().lock

    fun startSecondFactorFlow(
        sessionId: SessionId,
        password: ByteArray,
        secondFactorCode: String,
        isTwoPassModeNeeded: Boolean,
        requiredAccountType: AccountType
    ) {
        performSecondFactor(sessionId, secondFactorCode)
            .onProcessing { secondFactorState.post(it) }
            .onSecondFactorSuccess { success ->
                accountWorkflow.handleSecondFactorSuccess(sessionId, success.scopeInfo.scopes)
                // No more steps -> directly setup user.
                getUser(sessionId)
                    .onSuccess { userResult ->
                        onUserDetails(
                            sessionId,
                            password,
                            userResult.user,
                            success.scopeInfo,
                            isTwoPassModeNeeded,
                            requiredAccountType
                        )
                    }
                    .onError { error ->
                        secondFactorState.post(PerformSecondFactor.State.Error.FetchUser(error))
                    }
                    .launchIn(viewModelScope)
            }
            .onError { secondFactorState.post(it) }
            .launchIn(viewModelScope)
    }

    /**
     * Execute a routine when user details result is back from the API.
     */
    private suspend fun onUserDetails(
        sessionId: SessionId,
        password: ByteArray,
        user: User,
        scopeInfo: ScopeInfo,
        isTwoPassModeNeeded: Boolean,
        requiredAccountType: AccountType
    ) {
        val isTwoPass = if (isTwoPassModeNeeded && user.keys.isEmpty()) {
            // This is because of a bug on the API, where accounts with no keys return PasswordMode = 2.
            accountWorkflow.handleTwoPassModeSuccess(sessionId)
            false
        } else {
            isTwoPassModeNeeded
        }
        if (!isTwoPass && user.keys.isNotEmpty()) {
            // Raise Success.SecondFactor.
            setupUser(password, sessionId, scopeInfo, isTwoPass)
        } else {
            if (user.keys.isEmpty() && !user.addresses.satisfiesAccountType(requiredAccountType)) {
                // we upgrade it
                upgradeUsernameOnlyAccount(
                    sessionId = sessionId,
                    username = user.name!!, // for these accounts [AccountType.Username], name should always be present.
                    passphrase = password,
                    scopeInfo = scopeInfo,
                    isTwoPassModeNeeded = isTwoPass
                )
            } else {
                secondFactorState.post(
                    PerformSecondFactor.State.Success.SecondFactor(sessionId, scopeInfo, user, isTwoPass)
                )
            }
        }
    }

    private fun setupUser(
        password: ByteArray,
        sessionId: SessionId,
        scopeInfo: ScopeInfo,
        isTwoPassModeNeeded: Boolean
    ) {
        performUserSetup(sessionId, password)
            .onSuccess { success ->
                secondFactorState.post(
                    PerformSecondFactor.State.Success.UserSetup(
                        sessionId,
                        scopeInfo,
                        success.user,
                        isTwoPassModeNeeded
                    )
                )
            }
            .onError { error ->
                accountWorkflow.handleSecondFactorFailed(sessionId)
                secondFactorState.post(PerformSecondFactor.State.Error.UserSetup(error))
            }
            .launchIn(viewModelScope)
    }

    private fun upgradeUsernameOnlyAccount(
        sessionId: SessionId,
        username: String,
        passphrase: ByteArray,
        scopeInfo: ScopeInfo,
        isTwoPassModeNeeded: Boolean
    ) {
        updateUsernameOnlyAccount(sessionId = sessionId, username = username, passphrase = passphrase)
            .onSuccess { setupUser(passphrase, sessionId, scopeInfo, isTwoPassModeNeeded) }
            .onError {
                secondFactorState.post(PerformSecondFactor.State.Error.AccountUpgrade(it))
            }
            .launchIn(viewModelScope)
    }

    fun stopSecondFactorFlow(
        sessionId: SessionId
    ): Job = viewModelScope.launch { accountWorkflow.handleSecondFactorFailed(sessionId) }
}
