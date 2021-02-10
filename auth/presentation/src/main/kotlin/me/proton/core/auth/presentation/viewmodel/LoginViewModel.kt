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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserType
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class LoginViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performLogin: PerformLogin,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val setupOriginalAddress: SetupOriginalAddress
) : ProtonViewModel(), ViewStateStoreScope {

    val loginState = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        sealed class Success : State() {
            data class UserUnLocked(val sessionInfo: SessionInfo) : Success()
        }

        sealed class Need : State() {
            object ChangePassword : Need()
            data class SecondFactor(val sessionInfo: SessionInfo) : Need()
            data class TwoPassMode(val sessionInfo: SessionInfo) : Need()
            data class ChooseUsername(val sessionInfo: SessionInfo) : Need()
        }

        sealed class Error : State() {
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class Message(val message: String?) : Error()
        }
    }

    fun startLoginWorkflow(
        username: String,
        password: ByteArray,
        requiredUserType: UserType
    ) = flow {
        emit(State.Processing)

        val sessionInfo = performLogin.invoke(username, password)

        // Storing the session is mandatory for executing subsequent requests.
        handleSessionInfo(sessionInfo)

        // If SecondFactorNeeded, we cannot proceed without.
        if (sessionInfo.isSecondFactorNeeded) {
            emit(State.Need.SecondFactor(sessionInfo))
            return@flow
        }

        // Check if setup keys is needed and if it can be done directly.
        when (setupAccountCheck.invoke(sessionInfo.userId, sessionInfo.isTwoPassModeNeeded, requiredUserType)) {
            is SetupAccountCheck.Result.TwoPassNeeded -> State.Need.TwoPassMode(sessionInfo)
            is SetupAccountCheck.Result.ChangePasswordNeeded -> changePassword(sessionInfo)
            is SetupAccountCheck.Result.NoSetupNeeded -> unlockUserPrimaryKey(sessionInfo, password)
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> setupPrimaryKeys(sessionInfo, password)
            is SetupAccountCheck.Result.SetupOriginalAddressNeeded -> setupOriginalAddress(sessionInfo, password)
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> chooseUsername(sessionInfo, password)
        }.let {
            emit(it)
        }
    }.catch { error ->
        loginState.post(State.Error.Message(error.message))
    }.onEach { state ->
        loginState.post(state)
    }.launchIn(viewModelScope)

    private suspend fun changePassword(
        sessionInfo: SessionInfo
    ): State {
        accountWorkflow.handleAccountChangePasswordNeeded(sessionInfo.userId)
        return State.Need.ChangePassword
    }

    private suspend fun unlockUserPrimaryKey(
        session: SessionInfo,
        password: ByteArray
    ): State {
        val result = unlockUserPrimaryKey.invoke(session.userId, password)
        return if (result is UserManager.UnlockResult.Success) {
            accountWorkflow.handleAccountReady(session.userId)
            State.Success.UserUnLocked(session)
        } else {
            accountWorkflow.handleAccountUnlockFailed(session.userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun setupPrimaryKeys(
        session: SessionInfo,
        password: ByteArray
    ): State {
        setupPrimaryKeys.invoke(session.userId, password)
        return unlockUserPrimaryKey(session, password)
    }

    private suspend fun setupOriginalAddress(
        session: SessionInfo,
        password: ByteArray
    ): State {
        val state = unlockUserPrimaryKey(session, password)
        setupOriginalAddress.invoke(session.userId)
        return state
    }

    private suspend fun chooseUsername(
        session: SessionInfo,
        password: ByteArray
    ): State {
        val state = unlockUserPrimaryKey(session, password)
        return if (state is State.Success.UserUnLocked) {
            accountWorkflow.handleAccountCreateAddressNeeded(session.userId)
            State.Need.ChooseUsername(session)
        } else {
            state
        }
    }

    private suspend fun handleSessionInfo(sessionInfo: SessionInfo) {
        val sessionState =
            if (sessionInfo.isSecondFactorNeeded) SessionState.SecondFactorNeeded else SessionState.Authenticated
        val accountState =
            if (sessionInfo.isTwoPassModeNeeded) AccountState.TwoPassModeNeeded else AccountState.NotReady

        val account = Account(
            username = sessionInfo.username,
            userId = sessionInfo.userId,
            email = null,
            sessionId = sessionInfo.sessionId,
            state = accountState,
            sessionState = sessionState
        )
        val session = Session(
            sessionId = sessionInfo.sessionId,
            accessToken = sessionInfo.accessToken,
            refreshToken = sessionInfo.refreshToken,
            scopes = sessionInfo.scopes
        )
        accountWorkflow.handleSession(account, session)
    }
}
