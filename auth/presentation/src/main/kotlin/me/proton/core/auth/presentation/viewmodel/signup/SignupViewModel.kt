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

import androidx.activity.ComponentActivity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.toCreateUserType
import me.proton.core.auth.domain.usecase.signup.PerformCreateExternalEmailUser
import me.proton.core.auth.domain.usecase.signup.PerformCreateUser
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.viewmodel.AuthViewModel
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.humanverification.presentation.onHumanVerificationFailed
import me.proton.core.network.domain.session.HumanVerificationListener
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.exhaustive

internal class SignupViewModel @ViewModelInject constructor(
    private val performCreateUser: PerformCreateUser,
    private val performCreateExternalEmailUser: PerformCreateExternalEmailUser,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val humanVerificationListener: HumanVerificationListener,
    humanVerificationManager: HumanVerificationManager,
    humanVerificationOrchestrator: HumanVerificationOrchestrator
) : AuthViewModel(humanVerificationManager, humanVerificationOrchestrator) {

    // region private properties
    private val _inputState = MutableSharedFlow<InputState>(extraBufferCapacity = 10)
    private val _userCreationState = MutableStateFlow<State>(State.Idle)
    private var _recoveryMethod: RecoveryMethod? = null
    private lateinit var _password: EncryptedString

    // endregion
    // region public properties
    val userCreationState = _userCreationState.asStateFlow()
    val inputState = _inputState.asSharedFlow()

    var currentAccountType: AccountType = AccountType.Internal
    var username: String? = null
    var domain: String? = null
    var externalEmail: String? = null

    var password: String
        get() = _password.decryptWith(keyStoreCrypto)
        set(value) {
            _password = value.encryptWith(keyStoreCrypto)
        }

    override val recoveryEmailAddress: String?
        get() = if (_recoveryMethod?.type == RecoveryMethodType.EMAIL) _recoveryMethod?.destination else null
    // endregion

    // region state classes
    sealed class InputState {
        object Ready : InputState()
    }

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class Success(val user: User) : State()
        sealed class Error : State() {
            object HumanVerification : Error()
            data class Message(val message: String?) : Error()
        }
    }
    // endregion

    // region public API
    fun getLoginUsername() = when (currentAccountType) {
        AccountType.Username,
        AccountType.Internal -> username
        AccountType.External -> externalEmail
    }.exhaustive

    fun observeHumanVerification(context: ComponentActivity) = handleHumanVerificationState(context)
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

    /**
     * Starts the user creation flow. This function automatically decides what kind of user to create based on the
     * previously set [AccountType].
     * @see currentAccountType public property
     */
    suspend fun startCreateUserWorkflow() {
        when (currentAccountType) {
            AccountType.Username,
            AccountType.Internal -> createUser()
            AccountType.External -> createExternalUser()
        }.exhaustive
    }
    // endregion

    // region private functions
    private suspend fun createUser() = flow {
        val username = requireNotNull(username) { "Username is not set." }
        require(this@SignupViewModel::_password.isInitialized) { "Password is not set (initialized)." }
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
            username = username, password = _password, recoveryEmail = verification.first,
            recoveryPhone = verification.second, referrer = null, type = currentAccountType.toCreateUserType()
        )
        emit(State.Success(result))
    }.catch { error ->
        _userCreationState.tryEmit(State.Error.Message(error.message))
    }.onEach {
        _userCreationState.tryEmit(it)
    }.launchIn(viewModelScope)

    private suspend fun createExternalUser() = flow {
        val externalEmail = requireNotNull(externalEmail) { "External email is not set." }
        require(this@SignupViewModel::_password.isInitialized) { "Password is not set (initialized)." }
        emit(State.Processing)
        emit(
            State.Success(
                performCreateExternalEmailUser(
                    email = externalEmail,
                    password = _password,
                    referrer = null
                )
            )
        )
        externalAccountCreationDone()
    }.catch { error ->
        _userCreationState.tryEmit(State.Error.Message(error.message))
        externalAccountCreationDone()
    }.onEach {
        _userCreationState.tryEmit(it)
    }.launchIn(viewModelScope)

    private suspend fun externalAccountCreationDone() {
        humanVerificationListener.onExternalAccountHumanVerificationDone()
    }
    // endregion
}
