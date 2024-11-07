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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.fido2AuthenticationOptions
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.primaryKeyExists
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.fido.domain.usecase.toFidoStatus
import me.proton.core.auth.presentation.LogTag
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.observability.domain.metrics.LoginSecondFactorFidoLaunchResultTotal
import me.proton.core.observability.domain.metrics.LoginSecondFactorFidoSignResultTotal
import me.proton.core.observability.domain.metrics.LoginSecondFactorSubmissionTotal
import me.proton.core.observability.domain.metrics.LoginSecondFactorSubmissionTotal.SecondFactorProofType
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import me.proton.core.util.kotlin.retryOnceWhen
import javax.inject.Inject

@HiltViewModel
class SecondFactorViewModel @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val performSecondFactor: PerformSecondFactor,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val sessionProvider: SessionProvider,
    private val accountManager: AccountManager,
    private val isFido2Enabled: IsFido2Enabled,
    override val observabilityManager: ObservabilityManager,
) : ProtonViewModel(), ObservabilityContext {

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val state = _state.asSharedFlow()

    sealed class State {
        data class Idle(val fido2AuthenticationOptions: Fido2AuthenticationOptions?) : State()
        object Processing : State()
        data class AccountSetupResult(val result: PostLoginAccountSetup.Result) : State()

        sealed class Error : State() {
            data class Unrecoverable(val message: String?) : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    fun setup(userId: UserId) = flow {
        when {
            !isFido2Enabled(userId = null) -> emit(State.Idle(fido2AuthenticationOptions = null))
            else -> {
                val account = accountManager.getAccount(userId).firstOrNull()
                emit(State.Idle(account?.details?.session?.fido2AuthenticationOptions))
            }
        }
    }.catch { _ ->
        emit(State.Idle(fido2AuthenticationOptions = null))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

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
    ) = startSecondFactorFlow(
        userId = userId,
        encryptedPassword = encryptedPassword,
        requiredAccountType = requiredAccountType,
        isTwoPassModeNeeded = isTwoPassModeNeeded,
        proof = SecondFactorProof.SecondFactorCode(secondFactorCode)
    )

    fun startSecondFactorFlow(
        userId: UserId,
        encryptedPassword: EncryptedString,
        requiredAccountType: AccountType,
        isTwoPassModeNeeded: Boolean,
        proof: SecondFactorProof
    ) = flowWithResultContext(allowDuplicateResultKey = true) {
        onResultEnqueueObservability("performSecondFactor") { onPerformSecondFactorResult(proof, this) }

        emit(State.Processing)

        val sessionId = sessionProvider.getSessionId(userId)
        if (sessionId == null) {
            emit(State.Error.Unrecoverable("No session for this user."))
            return@flowWithResultContext
        }

        val scopeInfo = performSecondFactor.invoke(sessionId, proof)
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

    private fun onPerformSecondFactorResult(proof: SecondFactorProof, result: Result<*>): ObservabilityData {
        val type = when (proof) {
            is SecondFactorProof.Fido2 -> SecondFactorProofType.securityKey
            is SecondFactorProof.SecondFactorCode -> SecondFactorProofType.totp
            is SecondFactorProof.SecondFactorSignature -> SecondFactorProofType.u2f
        }
        return LoginSecondFactorSubmissionTotal(result, type)
    }

    internal fun onFidoLaunchResult(result: PerformTwoFaWithSecurityKey.LaunchResult) {
        observabilityManager.enqueue(LoginSecondFactorFidoLaunchResultTotal(result.toFidoStatus()))
    }

    internal fun onFidoSignResult(result: PerformTwoFaWithSecurityKey.Result) {
        observabilityManager.enqueue(LoginSecondFactorFidoSignResultTotal(result.toFidoStatus()))
    }

    internal fun onScreenView(screenId: LoginScreenViewTotal.ScreenId) {
        observabilityManager.enqueue(LoginScreenViewTotal(screenId))
    }

    companion object {
        const val HTTP_ERROR_UNAUTHORIZED = 401
        const val HTTP_ERROR_BAD_REQUEST = 400
    }
}
