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

package me.proton.core.auth.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.usecase.CreateLoginSsoSession
import me.proton.core.auth.domain.usecase.GetAuthInfoSso
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginSsoAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.isSwitchToSrp
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginAuthWithSsoTotal
import me.proton.core.observability.domain.metrics.LoginObtainSsoChallengeTokenTotal
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderPageLoadTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderResultTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderResultTotal.Status
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject

@HiltViewModel
class LoginSsoViewModel @Inject constructor(
    private val requiredAccountType: AccountType,
    private val getAuthInfoSso: GetAuthInfoSso,
    private val createLoginSsoSession: CreateLoginSsoSession,
    private val postLoginSsoAccountSetup: PostLoginSsoAccountSetup,
    override val observabilityManager: ObservabilityManager
) : ViewModel(), ObservabilityContext {

    private val mutableState = MutableStateFlow<State>(State.Idle)
    val state = mutableState.asStateFlow()

    sealed class State {
        object Idle : State()
        data class Processing(val cancellable: Boolean = true) : State()
        data class SignInWithSrp(val error: Throwable) : State()
        data class StartToken(val token: String) : State()
        data class AccountSetupResult(
            val userId: UserId,
            val result: PostLoginAccountSetup.Result
        ) : State()

        data class Error(val error: Throwable) : State()
    }

    fun startLoginWorkflow(
        email: String,
    ) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("getAuthInfoSso") { LoginObtainSsoChallengeTokenTotal(this) }
        flow {
            emit(State.Processing())
            val result = getAuthInfoSso(sessionId = null, email = email)
            emit(State.StartToken(result.token))
        }.catchWhen(Throwable::isSwitchToSrp) {
            emit(State.SignInWithSrp(it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it))
        }.onEach { state ->
            mutableState.emit(state)
        }.collect()
    }

    fun onIdentityProviderStarted() {
        mutableState.tryEmit(State.Processing())
    }

    fun onIdentityProviderSuccess(
        email: String,
        url: String,
    ) = viewModelScope.launchWithResultContext {
        enqueueObservability(LoginSsoIdentityProviderResultTotal(Status.success))
        onResultEnqueueObservability("performLoginSso") { LoginAuthWithSsoTotal(this) }

        flow {
            emit(State.Processing(cancellable = false))
            // Ex: url = "https://app-api.proton.domain/sso/login#token=token&uid=uid"
            // Ex: url = "proton://app-api.proton.domain/sso/login#token=token&uid=uid"
            val params = url.toUri().fragment?.split("&")?.associate { param ->
                val (key, value) = param.split("=")
                Pair(key, value)
            }
            val token = params?.getValue("token")
            val sessionInfo = createLoginSsoSession(email, requireNotNull(token), requiredAccountType)
            val result = postLoginSsoAccountSetup(sessionInfo.userId)
            emit(State.AccountSetupResult(sessionInfo.userId, result))
        }.catchWhen(Throwable::isSwitchToSrp) {
            emit(State.SignInWithSrp(it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it))
        }.onEach { state ->
            mutableState.emit(state)
        }.collect()
    }

    fun onIdentityProviderError() {
        enqueueObservability(LoginSsoIdentityProviderResultTotal(Status.error))
        mutableState.tryEmit(State.Idle)
    }

    fun onIdentityProviderCancel() {
        val state = mutableState.value
        if (state is State.Processing && !state.cancellable) return
        enqueueObservability(LoginSsoIdentityProviderResultTotal(Status.cancel))
        mutableState.tryEmit(State.Idle)
    }

    fun onIdentityProviderPageLoad(errorCode: Int?) {
        observabilityManager.enqueue(LoginSsoIdentityProviderPageLoadTotal(errorCode))
    }

    fun onScreenView(screenId: LoginScreenViewTotal.ScreenId) {
        observabilityManager.enqueue(LoginScreenViewTotal(screenId))
    }
}
