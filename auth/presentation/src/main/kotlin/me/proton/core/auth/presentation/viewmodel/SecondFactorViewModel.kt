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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.primaryKeyExists
import me.proton.core.auth.presentation.LogTag
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.retryOnceWhen
import javax.inject.Inject

@HiltViewModel
class SecondFactorViewModel @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val sessionProvider: SessionProvider,
) : ProtonViewModel() {

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val state = _state.asSharedFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class AccountSetupResult(val result: PostLoginAccountSetup.Result) : State()

        sealed class Error : State() {
            data class Unrecoverable(val message: String?) : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    fun stopSecondFactorFlow(userId: UserId): Job = viewModelScope.launch {
        val sessionId = sessionProvider.getSessionId(userId)
        sessionId?.let { accountWorkflow.handleSecondFactorFailed(sessionId) }
    }

    fun startSecondFactorFlow(
        userId: UserId,
        encryptedPassword: EncryptedString,
        requiredAccountType: AccountType,
        isTwoPassModeNeeded: Boolean,
        secondFactorCode: String
    ) = flow {
        emit(State.Processing)

        val sessionId = sessionProvider.getSessionId(userId)
        if (sessionId == null) {
            emit(State.Error.Unrecoverable("No session for this user."))
            return@flow
        }

        val scopeInfo = performSecondFactor.invoke(sessionId, secondFactorCode)
        accountWorkflow.handleSecondFactorSuccess(sessionId, scopeInfo.scopes)

        val result = postLoginAccountSetup(
            userId = userId,
            encryptedPassword = encryptedPassword,
            requiredAccountType = requiredAccountType,
            isSecondFactorNeeded = false,
            isTwoPassModeNeeded = isTwoPassModeNeeded,
            temporaryPassword = false
        )
        emit(State.AccountSetupResult(result))
    }.retryOnceWhen(Throwable::primaryKeyExists) {
        CoreLogger.e(LogTag.FLOW_ERROR_RETRY, it, "Retrying second factor flow")
    }.catch { error ->
        if (error.isUnrecoverableError()) {
            stopSecondFactorFlow(userId).join()
            emit(State.Error.Unrecoverable(error.message))
        } else {
            emit(State.Error.Message(error))
        }
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

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
