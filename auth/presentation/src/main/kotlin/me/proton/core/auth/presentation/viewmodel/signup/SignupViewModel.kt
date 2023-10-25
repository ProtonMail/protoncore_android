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

package me.proton.core.auth.presentation.viewmodel.signup

import android.os.Parcelable
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.signup.PerformCreateExternalEmailUser
import me.proton.core.auth.domain.usecase.signup.PerformCreateUser
import me.proton.core.auth.domain.usecase.signup.SignupChallengeConfig
import me.proton.core.auth.domain.usecase.userAlreadyExists
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.entity.signup.SubscriptionDetails
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.humanverification.domain.HumanVerificationExternalInput
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupAccountCreationTotal
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.observability.domain.metrics.common.AccountTypeLabels
import me.proton.core.observability.domain.metrics.common.toObservabilityAccountType
import me.proton.core.payment.domain.usecase.CanUpgradeToPaid
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.IsDynamicPlanEnabled
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.presentation.savedstate.flowState
import me.proton.core.presentation.savedstate.state
import me.proton.core.user.domain.entity.createUserType
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.withResultContext
import javax.inject.Inject

@HiltViewModel
internal class SignupViewModel @Inject constructor(
    private val humanVerificationExternalInput: HumanVerificationExternalInput,
    private val performCreateUser: PerformCreateUser,
    private val performCreateExternalEmailUser: PerformCreateExternalEmailUser,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val plansOrchestrator: PlansOrchestrator,
    private val paymentsOrchestrator: PaymentsOrchestrator,
    private val performLogin: PerformLogin,
    private val challengeManager: ChallengeManager,
    private val challengeConfig: SignupChallengeConfig,
    override val observabilityManager: ObservabilityManager,
    private val canUpgradeToPaid: CanUpgradeToPaid,
    private val isDynamicPlanEnabled: IsDynamicPlanEnabled,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ObservabilityContext {

    private val _state by savedStateHandle.flowState(
        mutableSharedFlow = MutableSharedFlow<State>(replay = 1).apply { tryEmit(State.Idle) },
        coroutineScope = viewModelScope,
        onStateRestored = this::onUserCreationStateRestored
    )

    private var _recoveryMethod: RecoveryMethod? by savedStateHandle.state(null)
    private var _password: EncryptedString? by savedStateHandle.state(null)

    var subscriptionDetails: SubscriptionDetails? by savedStateHandle.state(null)
    var currentAccountType: AccountType by savedStateHandle.state(AccountType.Internal)
    var username: String? by savedStateHandle.state(null)
    var domain: String? by savedStateHandle.state(null)
    var externalEmail: String? by savedStateHandle.state(null)

    val state by lazy { _state.asSharedFlow() }

    sealed class State : Parcelable {
        @Parcelize
        object Idle : State()

        @Parcelize
        object PreloadingPlans: State()

        @Parcelize
        data class CreateUserInputReady(
            val paidOptionAvailable: Boolean,
            val isDynamicPlanEnabled: Boolean
        ) : State()

        @Parcelize
        object CreateUserProcessing : State()

        @Parcelize
        data class CreateUserSuccess(val userId: String, val username: String, val password: EncryptedString) : State()

        sealed class Error : State() {
            @Parcelize
            object CreateUserCanceled : Error()

            @Parcelize
            object PlanChooserCanceled : Error()

            @Parcelize
            data class Message(val message: String?) : Error()
        }
    }

    fun onScreenView(screenId: SignupScreenViewTotalV1.ScreenId) {
        enqueueObservability(SignupScreenViewTotalV1(screenId))
    }

    private fun setExternalRecoveryEmail(recoveryMethod: RecoveryMethod?) {
        humanVerificationExternalInput.recoveryEmail = recoveryMethod?.destination.takeIf {
            recoveryMethod?.type == RecoveryMethodType.EMAIL
        }
    }

    fun setPassword(password: String?) {
        _password = password?.encrypt(keyStoreCrypto)
    }

    fun skipRecoveryMethod() = setRecoveryMethod(null)

    fun setRecoveryMethod(recoveryMethod: RecoveryMethod?) = flow {
        emit(State.PreloadingPlans)
        _recoveryMethod = recoveryMethod
        setExternalRecoveryEmail(recoveryMethod)
        emit(
            State.CreateUserInputReady(
                paidOptionAvailable = canUpgradeToPaid(),
                isDynamicPlanEnabled = isDynamicPlanEnabled(userId = null)
            )
        )
    }.catch {
        emit(State.Error.Message(it.message))
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)

    fun onCreateUserCancelled() {
        _state.tryEmit(State.Error.CreateUserCanceled)
    }

    fun onPlanChooserCancel() {
        _state.tryEmit(State.Error.PlanChooserCanceled)
    }

    fun startCreateUserWorkflow(): Job = flow {
        emit(State.CreateUserProcessing)
        when (currentAccountType) {
            AccountType.Username,
            AccountType.Internal -> {
                val username = requireNotNull(username) { "Username is not set." }
                val password = requireNotNull(_password) { "Password is not set (initialized)." }
                emitAll(createUser(username, password, domain, currentAccountType))
            }
            AccountType.External -> {
                val email = requireNotNull(externalEmail) { "External email is not set." }
                val password = requireNotNull(_password) { "Password is not set (initialized)." }
                emitAll(createExternalUser(email, password))
            }
        }
    }.catch { error ->
        emit(State.Error.Message(error.message))
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)

    fun register(caller: ActivityResultCaller) {
        plansOrchestrator.register(caller)
        paymentsOrchestrator.register(caller)
    }

    fun onFinish() {
        viewModelScope.launch {
            challengeManager.resetFlow(challengeConfig.flowName)
        }
    }

    fun onSignupCompleted() {
        _state.tryEmit(State.Idle)
    }

    private fun createUser(
        username: String,
        encryptedPassword: EncryptedString,
        domain: String?,
        accountType: AccountType
    ) = flow<State> {
        val destination = _recoveryMethod?.destination
        val type = _recoveryMethod?.type
        val recoveryEmail = destination.takeIf { type == RecoveryMethodType.EMAIL }
        val recoveryPhone = destination.takeIf { type == RecoveryMethodType.SMS }
        val result = withResultContext {
            onResultEnqueueObservability("createUser") {
                SignupAccountCreationTotal(this, accountType.toObservabilityAccountType())
            }
            performCreateUser(
                username = username,
                password = encryptedPassword,
                recoveryEmail = recoveryEmail,
                recoveryPhone = recoveryPhone,
                referrer = null,
                type = currentAccountType.createUserType(),
                domain = domain
            )
        }
        emit(State.CreateUserSuccess(result.id, username, encryptedPassword))
    }.catchWhen(Throwable::userAlreadyExists) {
        val userId = performLogin.invoke(username, encryptedPassword).userId
        emit(State.CreateUserSuccess(userId.id, username, encryptedPassword))
    }

    private fun createExternalUser(externalEmail: String, encryptedPassword: EncryptedString) = flow<State> {
        val userId = withResultContext {
            onResultEnqueueObservability("createExternalEmailUser") {
                SignupAccountCreationTotal(this, AccountTypeLabels.external)
            }
            performCreateExternalEmailUser(
                email = externalEmail,
                password = encryptedPassword,
                referrer = null
            )
        }
        emit(State.CreateUserSuccess(userId.id, externalEmail, encryptedPassword))
    }.catchWhen(Throwable::userAlreadyExists) {
        val userId = performLogin.invoke(externalEmail, encryptedPassword).userId
        emit(State.CreateUserSuccess(userId.id, externalEmail, encryptedPassword))
    }

    private fun onUserCreationStateRestored(state: State) {
        if (state == State.CreateUserProcessing) {
            // The view model was destroyed while creating the account; try to resume the process:
            startCreateUserWorkflow()
        }
    }
}
