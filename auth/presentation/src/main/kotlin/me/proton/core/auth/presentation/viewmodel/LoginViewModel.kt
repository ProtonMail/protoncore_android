/*
 * Copyright (c) 2023 Proton AG
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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.BillingDetails
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.usecase.IsSsoEnabled
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.primaryKeyExists
import me.proton.core.auth.presentation.LogTag
import me.proton.core.auth.presentation.telemetry.ProductMetricsDelegateAuth
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.ResponseCodes.APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS
import me.proton.core.network.domain.ResponseCodes.AUTH_SWITCH_TO_SSO
import me.proton.core.network.domain.isPotentialBlocking
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.telemetry.domain.TelemetryContext
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.retryOnceWhen
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountWorkflow: AccountWorkflowHandler,
    private val createLoginSession: CreateLoginSession,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    isSsoEnabled: IsSsoEnabled,
    override val observabilityManager: ObservabilityManager,
    override val telemetryManager: TelemetryManager
) : ViewModel(), ProductMetricsDelegateAuth, ObservabilityContext, TelemetryContext {

    override val productGroup: String = "account.any.signup"
    override val productFlow: String = "mobile_signup_full"
    override var userId: UserId?
        get() = savedStateHandle.get<String>(STATE_USER_ID)?.let { UserId(it) }
        set(value) { savedStateHandle[STATE_USER_ID] = value?.id }

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val isSsoEnabled: Boolean = isSsoEnabled()
    val state = _state.asSharedFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class SignInWithSso(val email: String, val error: Throwable) : State()
        data class AccountSetupResult(val result: PostLoginAccountSetup.Result) : State()
        data class Error(val error: Throwable, val isPotentialBlocking: Boolean) : State()
        data class InvalidPassword(val error: Throwable) : State()
        data class ExternalAccountNotSupported(val error: Throwable) : State()
    }

    fun stopLoginWorkflow(): Job = viewModelScope.launch {
        userId?.let { accountWorkflow.handleAccountDisabled(it) }
    }

    fun startLoginWorkflow(
        username: String,
        password: String,
        requiredAccountType: AccountType,
        billingDetails: BillingDetails? = null,
        loginMetricData: ((Result<*>) -> ObservabilityData)? = null,
        unlockUserMetricData: ((Result<*>) -> ObservabilityData)? = null,
        userCheckMetricData: ((Result<*>) -> ObservabilityData)? = null
    ): Job = startLoginWorkflowWithEncryptedPassword(
        username = username,
        encryptedPassword = password.encrypt(keyStoreCrypto),
        requiredAccountType = requiredAccountType,
        billingDetails = billingDetails,
        loginMetricData = loginMetricData,
        unlockUserMetricData = unlockUserMetricData,
        userCheckMetricData = userCheckMetricData
    )

    fun startLoginWorkflowWithEncryptedPassword(
        username: String,
        encryptedPassword: EncryptedString,
        requiredAccountType: AccountType,
        billingDetails: BillingDetails? = null,
        loginMetricData: ((Result<*>) -> ObservabilityData)? = null,
        unlockUserMetricData: ((Result<*>) -> ObservabilityData)? = null,
        userCheckMetricData: ((Result<*>) -> ObservabilityData)? = null
    ) = viewModelScope.launchWithResultContext {
        loginMetricData?.let {
            onResultEnqueueObservability("performLogin") { it(this) }
        }
        unlockUserMetricData?.let {
            onResultEnqueueObservability("unlockUserPrimaryKey") { it(this) }
        }
        userCheckMetricData?.let {
            onResultEnqueueObservability("defaultUserCheck") { it(this) }
        }
        onResultEnqueueTelemetry("performLogin") {
            toTelemetryEvent("be.signin.auth", requiredAccountType)
        }

        flow {
            emit(State.Processing)

            val sessionInfo = createLoginSession(username, encryptedPassword, requiredAccountType)
            userId = sessionInfo.userId

            val result = postLoginAccountSetup(
                userId = sessionInfo.userId,
                encryptedPassword = encryptedPassword,
                requiredAccountType = requiredAccountType,
                isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
                isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
                temporaryPassword = sessionInfo.temporaryPassword,
                billingDetails = billingDetails
            )
            emit(State.AccountSetupResult(result))
        }.retryOnceWhen(Throwable::primaryKeyExists) {
            CoreLogger.e(LogTag.FLOW_ERROR_RETRY, it, "Retrying login flow")
        }.catchWhen(Throwable::isWrongPassword) {
            emit(State.InvalidPassword(it))
        }.catchWhen(Throwable::isExternalAccountNotSupported) {
            emit(State.ExternalAccountNotSupported(it))
        }.catchWhen(Throwable::isSwitchToSso) {
            emit(State.SignInWithSso(username, it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it, it.isPotentialBlocking()))
        }.collect { state ->
            _state.tryEmit(state)
        }
    }

    companion object {
        const val STATE_USER_ID = "userId"
    }
}

/**
 * Note: server may also return this error, when an account doesn't exist,
 * i.e. there is no account with given username/email.
 */
private fun Throwable.isWrongPassword(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == ResponseCodes.PASSWORD_WRONG
}

private fun Throwable.isExternalAccountNotSupported(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS
}

private fun Throwable.isSwitchToSso(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == AUTH_SWITCH_TO_SSO
}
