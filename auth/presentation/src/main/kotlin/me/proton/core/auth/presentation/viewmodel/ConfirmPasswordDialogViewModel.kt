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

package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.entity.Fido2Info
import me.proton.core.auth.domain.entity.SecondFactor
import me.proton.core.auth.domain.entity.SecondFactorMethod
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.usecase.GetAuthInfoSrp
import me.proton.core.auth.domain.usecase.scopes.ObtainLockedScope
import me.proton.core.auth.domain.usecase.scopes.ObtainPasswordScope
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.fido.domain.usecase.toStatus
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ConfirmPasswordFidoLaunchResultTotal
import me.proton.core.observability.domain.metrics.ConfirmPasswordFidoSignResultTotal
import me.proton.core.observability.domain.metrics.ConfirmPasswordSubmissionTotal
import me.proton.core.observability.domain.metrics.ConfirmPasswordSubmissionTotal.SecondFactorProofType
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import me.proton.core.util.kotlin.exhaustive
import me.proton.core.util.kotlin.takeIfNotEmpty
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class ConfirmPasswordDialogViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val getAuthInfoSrp: GetAuthInfoSrp,
    private val isFido2Enabled: IsFido2Enabled,
    private val obtainLockedScope: ObtainLockedScope,
    private val obtainPasswordScope: ObtainPasswordScope,
    private val missingScopeListener: MissingScopeListener,
    override val observabilityManager: ObservabilityManager
) : ProtonViewModel(), ObservabilityContext {
    var fido2Info: Fido2Info? = null

    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asSharedFlow()

    sealed class State {
        object Idle : State()
        object ProcessingSecondFactor : State()
        object ProcessingObtainScope : State()
        data class Success(val state: MissingScopeState) : State()
        data class SecondFactorResult(val methods: List<SecondFactorMethod>) : State()

        sealed class Error : State() {
            object InvalidAccount : Error()
            object Unknown : Error()
            data class General(val error: Throwable) : Error()
        }
    }

    fun checkForSecondFactorInput(userId: UserId, missingScope: Scope) = flow {
        emit(State.ProcessingSecondFactor)
        val account = accountManager.getAccount(userId).firstOrNull()
        if (account == null) {
            emit(State.Error.InvalidAccount)
            return@flow
        }
        val authInfo = getAuthInfoSrp(requireNotNull(account.sessionId), requireNotNull(account.username))
        val secondFactor = authInfo.secondFactor as? SecondFactor.Enabled
        val fido2Enabled = isFido2Enabled(userId)
        val secondFactorMethods = when (missingScope) {
            Scope.PASSWORD -> secondFactor?.supportedMethods.orEmpty()
            Scope.LOCKED -> emptySet()
        }.filter {
            it != SecondFactorMethod.Authenticator || fido2Enabled
        }.sortedDescending()
        fido2Info = secondFactor?.fido2

        emit(State.SecondFactorResult(secondFactorMethods))
    }.catch { error ->
        emit(State.Error.General(error))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)

    fun unlock(
        userId: UserId,
        missingScope: Scope,
        password: String,
        secondFactorProof: SecondFactorProof? = null
    ) = flowWithResultContext {
        it.onResultEnqueueObservability("unlockUserForPasswordScope") {
            toConfirmPasswordSubmissionTotal(secondFactorProof)
        }

        send(State.ProcessingObtainScope)
        yield()

        val account = accountManager.getAccount(userId).firstOrNull()
        if (account == null) {
            send(State.Error.InvalidAccount)
            return@flowWithResultContext
        }
        val result = when (missingScope) {
            Scope.PASSWORD -> obtainPasswordScope(
                userId = account.userId,
                sessionId = requireNotNull(account.sessionId),
                username = requireNotNull(account.username),
                password = password.encrypt(keyStoreCrypto),
                secondFactorProof = secondFactorProof
            )

            Scope.LOCKED -> obtainLockedScope(
                userId = account.userId,
                sessionId = requireNotNull(account.sessionId),
                username = requireNotNull(account.username),
                password = password.encrypt(keyStoreCrypto)
            )
        }.exhaustive

        if (result) {
            send(State.Success(MissingScopeState.ScopeObtainSuccess))
        } else {
            send(State.Error.Unknown)
        }
    }.catch { error ->
        emit(State.Error.General(error))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)

    fun onConfirmPasswordResult(state: MissingScopeState?): Job = viewModelScope.launch {
        when (state) {
            MissingScopeState.ScopeObtainSuccess -> missingScopeListener.onMissingScopeSuccess()
            else -> missingScopeListener.onMissingScopeFailure()
        }.exhaustive
    }

    fun onLaunchResult(launchResult: PerformTwoFaWithSecurityKey.LaunchResult) {
        enqueueObservability(ConfirmPasswordFidoLaunchResultTotal(launchResult.toStatus()))
    }

    fun onSignResult(result: PerformTwoFaWithSecurityKey.Result) {
        enqueueObservability(ConfirmPasswordFidoSignResultTotal(result.toStatus()))
    }
}

private fun Result<*>.toConfirmPasswordSubmissionTotal(
    secondFactorProof: SecondFactorProof?
): ConfirmPasswordSubmissionTotal {
    val secondFactorProofType = when {
        secondFactorProof is SecondFactorProof.SecondFactorCode -> SecondFactorProofType.totp
        secondFactorProof is SecondFactorProof.Fido2 -> SecondFactorProofType.securityKey
        else -> SecondFactorProofType.none
    }
    return if (exceptionOrNull() is InvalidServerAuthenticationException) {
        ConfirmPasswordSubmissionTotal(
            ConfirmPasswordSubmissionTotal.Status.invalidServerAuthentication,
            secondFactorProofType
        )
    } else {
        ConfirmPasswordSubmissionTotal(this, secondFactorProofType)
    }
}
