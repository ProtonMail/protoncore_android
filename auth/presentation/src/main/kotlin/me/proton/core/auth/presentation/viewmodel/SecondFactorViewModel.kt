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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserType
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class SecondFactorViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val setupOriginalAddress: SetupOriginalAddress,
) : ProtonViewModel(), ViewStateStoreScope {

    val secondFactorState = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        sealed class Success : State() {
            data class UserUnLocked(val scopeInfo: ScopeInfo) : Success()
        }

        sealed class Need : State() {
            object ChangePassword : Need()
            data class TwoPassMode(val scopeInfo: ScopeInfo) : Need()
            data class ChooseUsername(val scopeInfo: ScopeInfo) : Need()
        }

        sealed class Error : State() {
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class Message(val message: String?) : Error()
            object Unrecoverable : Error()
        }
    }

    fun stopSecondFactorFlow(
        sessionId: SessionId
    ): Job = viewModelScope.launch { accountWorkflow.handleSecondFactorFailed(sessionId) }

    fun startSecondFactorFlow(
        password: ByteArray,
        requiredUserType: UserType,
        session: SessionResult,
        secondFactorCode: String
    ) = flow {
        emit(State.Processing)

        val sessionId = SessionId(session.sessionId)

        val scopeInfo = performSecondFactor.invoke(sessionId, secondFactorCode)
        accountWorkflow.handleSecondFactorSuccess(sessionId, scopeInfo.scopes)

        // Check if setup keys is needed and if it can be done directly.
        when (setupAccountCheck.invoke(session.sessionId, session.isTwoPassModeNeeded, requiredUserType)) {
            is SetupAccountCheck.Result.TwoPassNeeded -> State.Need.TwoPassMode(scopeInfo)
            is SetupAccountCheck.Result.ChangePasswordNeeded -> changePassword(session)
            is SetupAccountCheck.Result.NoSetupNeeded -> unlockUserPrimaryKey(session, scopeInfo, password)
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> setupPrimaryKeys(session, scopeInfo, password)
            is SetupAccountCheck.Result.SetupOriginalAddressNeeded -> setupOriginalAddress(session, scopeInfo, password)
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> chooseUsername(session, scopeInfo, password)
        }.let {
            emit(it)
        }
    }.catch { error ->
        if (error.isUnrecoverableError())
            secondFactorState.post(State.Error.Unrecoverable)
        else
            secondFactorState.post(State.Error.Message(error.message))
    }.onEach { state ->
        secondFactorState.post(state)
    }.launchIn(viewModelScope)

    private suspend fun changePassword(
        session: SessionResult
    ): State {
        accountWorkflow.handleAccountChangePasswordNeeded(UserId(session.userId))
        return State.Need.ChangePassword
    }

    private suspend fun unlockUserPrimaryKey(
        session: SessionResult,
        scopeInfo: ScopeInfo,
        password: ByteArray
    ): State {
        val result = unlockUserPrimaryKey.invoke(SessionId(session.sessionId), password)
        return if (result == UserManager.UnlockResult.Success) {
            accountWorkflow.handleAccountReady(UserId(session.userId))
            State.Success.UserUnLocked(scopeInfo)
        } else {
            accountWorkflow.handleAccountUnlockFailed(UserId(session.userId))
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }

    private suspend fun setupPrimaryKeys(
        session: SessionResult,
        scopeInfo: ScopeInfo,
        password: ByteArray
    ): State {
        setupPrimaryKeys.invoke(SessionId(session.sessionId), password)
        return unlockUserPrimaryKey(session, scopeInfo, password)
    }

    private suspend fun setupOriginalAddress(
        session: SessionResult,
        scopeInfo: ScopeInfo,
        password: ByteArray
    ): State {
        val state = unlockUserPrimaryKey(session, scopeInfo, password)
        setupOriginalAddress.invoke(SessionId(session.sessionId))
        return state
    }

    private suspend fun chooseUsername(
        session: SessionResult,
        scopeInfo: ScopeInfo,
        password: ByteArray
    ): State {
        val state = unlockUserPrimaryKey(session, scopeInfo, password)
        return if (state is State.Success.UserUnLocked) {
            accountWorkflow.handleAccountCreateAddressNeeded(UserId(session.userId))
            State.Need.ChooseUsername(scopeInfo)
        } else {
            state
        }
    }

    private fun Throwable.isUnrecoverableError(): Boolean {
        if (this is ApiException && error is ApiResult.Error.Http) {
            val httpCode = (error as ApiResult.Error.Http).httpCode
            return httpCode in listOf(HTTP_ERROR_UNAUTHORIZED, HTTP_ERROR_BAD_REQUEST)
        }
        return false
    }

    companion object {
        const val HTTP_ERROR_UNAUTHORIZED = 401
        const val HTTP_ERROR_BAD_REQUEST = 400
    }
}
