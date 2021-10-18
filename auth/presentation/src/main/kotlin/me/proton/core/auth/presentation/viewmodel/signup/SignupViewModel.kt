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

package me.proton.core.auth.presentation.viewmodel.signup

import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.signup.PerformCreateExternalEmailUser
import me.proton.core.auth.domain.usecase.signup.PerformCreateUser
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.entity.signup.SubscriptionDetails
import me.proton.core.auth.presentation.viewmodel.AuthViewModel
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.humanverification.presentation.onHumanVerificationFailed
import me.proton.core.humanverification.presentation.onHumanVerificationNeeded
import me.proton.core.humanverification.presentation.onHumanVerificationSucceeded
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.presentation.savedstate.flowState
import me.proton.core.presentation.savedstate.state
import me.proton.core.user.domain.entity.createUserType
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class SignupViewModel @Inject constructor(
    private val performCreateUser: PerformCreateUser,
    private val performCreateExternalEmailUser: PerformCreateExternalEmailUser,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val plansOrchestrator: PlansOrchestrator,
    private val paymentsOrchestrator: PaymentsOrchestrator,
    private val clientIdProvider: ClientIdProvider,
    private val humanVerificationManager: HumanVerificationManager,
    humanVerificationOrchestrator: HumanVerificationOrchestrator,
    savedStateHandle: SavedStateHandle
) : AuthViewModel(humanVerificationManager, humanVerificationOrchestrator) {

    // region private properties
    private var _currentAccountTypeOrdinal: Int by savedStateHandle.state(AccountType.Internal.ordinal)
    private val _inputState = MutableSharedFlow<InputState>(replay = 1)
    private val _userCreationState by savedStateHandle.flowState(
        MutableSharedFlow<State>(replay = 1),
        viewModelScope,
        onStateRestored = this::onUserCreationStateRestored
    )
    private var _recoveryMethod: RecoveryMethod? by savedStateHandle.state(null)
    private var _password: EncryptedString? by savedStateHandle.state(null)

    // endregion
    // region public properties
    var subscriptionDetails: SubscriptionDetails? by savedStateHandle.state(null)
    val userCreationState by lazy { _userCreationState.asSharedFlow() }
    val inputState by lazy { _inputState.asSharedFlow() }

    var currentAccountType: AccountType
        get() = AccountType.values()[_currentAccountTypeOrdinal]
        set(value) {
            _currentAccountTypeOrdinal = value.ordinal
        }
    var username: String? by savedStateHandle.state(null)
    var domain: String? by savedStateHandle.state(null)
    var externalEmail: String? by savedStateHandle.state(null)

    override val recoveryEmailAddress: String?
        get() = if (_recoveryMethod?.type == RecoveryMethodType.EMAIL) _recoveryMethod?.destination else null
    // endregion

    // region state classes
    sealed class InputState : Parcelable {
        @Parcelize
        object Ready : InputState()
    }

    sealed class State : Parcelable {
        @Parcelize
        object Idle : State()

        @Parcelize
        object Processing : State()

        @Parcelize
        data class Success(val userId: String, val loginUsername: String, val password: EncryptedString) : State()

        sealed class Error : State() {
            @Parcelize
            object HumanVerification : Error()

            @Parcelize
            object PlanChooserCancel : Error()

            @Parcelize
            data class Message(val message: String?) : Error()
        }
    }
    // endregion

    // region public API

    fun setPassword(password: String?) {
        _password = password?.encrypt(keyStoreCrypto)
    }

    fun observeHumanVerification(context: ComponentActivity) = handleHumanVerificationState(context)
        .onHumanVerificationSucceeded {
            _userCreationState.tryEmit(State.Processing)
        }
        .onHumanVerificationFailed {
            _userCreationState.tryEmit(State.Error.HumanVerification)
        }

    fun skipRecoveryMethod() = setRecoveryMethod(null)

    fun setRecoveryMethod(recoveryMethod: RecoveryMethod?) {
        _recoveryMethod = recoveryMethod
        _inputState.tryEmit(InputState.Ready)
    }

    fun setExternalAccountEmailValidationDone() {
        _inputState.tryEmit(InputState.Ready)
    }

    fun onPlanChooserCancel() {
        _userCreationState.tryEmit(State.Idle)
        _userCreationState.tryEmit(State.Error.PlanChooserCancel)
    }

    /**
     * Starts the user creation flow. This function automatically decides what kind of user to create based on the
     * previously set [AccountType].
     * @see currentAccountType public property
     */
    fun startCreateUserWorkflow() = viewModelScope.launch {
        _userCreationState.tryEmit(State.Idle)
        when (currentAccountType) {
            AccountType.Username,
            AccountType.Internal -> createUser()
            AccountType.External -> createExternalUser()
        }.exhaustive
    }

    fun startCreatePaidUserWorkflow(
        planName: String,
        planDisplayName: String,
        cycle: SubscriptionCycle,
        billingResult: BillingResult
    ) {
        val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
        subscriptionDetails = SubscriptionDetails(
            billingResult = billingResult,
            planName = planName,
            planDisplayName = planDisplayName,
            cycle = cycle
        )
        if (billingResult.paySuccess) {
            viewModelScope.launch {
                // update subscription details
                humanVerificationManager.addDetails(
                    details = BillingResult.paymentDetails(
                        clientId = clientId,
                        token = billingResult.token!!
                    )
                )
            }
            startCreateUserWorkflow()
        }
    }

    override fun register(context: FragmentActivity) {
        super.register(context)
        plansOrchestrator.register(context)
        paymentsOrchestrator.register(context)
    }

    // endregion

    // region private functions
    private suspend fun createUser() = flow {
        val username = requireNotNull(username) { "Username is not set." }
        val encryptedPassword = requireNotNull(_password) { "Password is not set (initialized)." }
        emit(State.Processing)

        val verification = _recoveryMethod?.let {
            val email = if (it.type == RecoveryMethodType.EMAIL) {
                it.destination
            } else null
            val phone = if (it.type == RecoveryMethodType.SMS) {
                it.destination
            } else null
            Pair(email, phone)
        } ?: run {
            Pair(null, null)
        }

        val result = performCreateUser(
            username = username, password = encryptedPassword, recoveryEmail = verification.first,
            recoveryPhone = verification.second, referrer = null, type = currentAccountType.createUserType()
        )
        emit(State.Success(result.userId.id, username, encryptedPassword))
    }.catch { error ->
        emit(State.Error.Message(error.message))
    }.onEach {
        _userCreationState.tryEmit(it)
    }.launchIn(viewModelScope)

    private suspend fun createExternalUser() = flow {
        val externalEmail = requireNotNull(externalEmail) { "External email is not set." }
        val encryptedPassword = requireNotNull(_password) { "Password is not set (initialized)." }
        emit(State.Processing)
        val user = performCreateExternalEmailUser(
            email = externalEmail,
            password = encryptedPassword,
            referrer = null
        )
        emit(State.Success(user.userId.id, externalEmail, encryptedPassword))
    }.catch { error ->
        emit(State.Error.Message(error.message))
    }.onEach {
        _userCreationState.tryEmit(it)
    }.launchIn(viewModelScope)

    private fun onUserCreationStateRestored(state: State) {
        if (state == State.Processing) {
            // The view model was destroyed while creating the account; try to resume the process:
            startCreateUserWorkflow()
        }
    }

    // endregion
}
