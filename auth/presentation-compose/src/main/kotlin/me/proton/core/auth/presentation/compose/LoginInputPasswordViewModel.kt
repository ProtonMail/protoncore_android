/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.presentation.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AuthSecret
import me.proton.core.auth.domain.LoginFlow
import me.proton.core.auth.domain.LoginState
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.ChangePassword
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.Error
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.Idle
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.NeedSrp
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.NeedSso
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.Processing
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.Success
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.UserCheckError
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.ValidationError
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.ExternalEmailNotSupported
import me.proton.core.auth.presentation.compose.LoginInputPasswordState.ExternalSsoNotSupported
import me.proton.core.auth.presentation.compose.LoginRoutes.Arg.getUsername
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.telemetry.domain.TelemetryContext
import me.proton.core.telemetry.domain.TelemetryManager
import javax.inject.Inject

@HiltViewModel
public class LoginInputPasswordViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val loginFlow: LoginFlow,
    public override val observabilityManager: ObservabilityManager,
    public override val telemetryManager: TelemetryManager
) : ViewModel(), ObservabilityContext, TelemetryContext {

    internal val username by lazy { requireNotNull(savedStateHandle.getUsername()) }

    private val mutableAction = MutableSharedFlow<LoginInputPasswordAction>()

    public val state: StateFlow<LoginInputPasswordState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is LoginInputPasswordAction.SetPassword -> onValidatePassword(action)
        }
    }.stateIn(viewModelScope, Lazily, Idle) // Lazily -> HumanVerification.

    public fun submit(action: LoginInputPasswordAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onValidatePassword(action: LoginInputPasswordAction.SetPassword) = flow {
        val isValid = InputValidationResult(action.password, ValidationType.Password).isValid
        when {
            !isValid -> emit(ValidationError)
            else -> emitAll(onSetPassword(action))
        }
    }

    private fun onSetPassword(action: LoginInputPasswordAction.SetPassword) = loginFlow.invoke(
        username = action.username,
        secret = AuthSecret.Srp(action.password)
    ).mapToLoginInputPasswordState()

    private fun Flow<LoginState>.mapToLoginInputPasswordState() = map {
        when (it) {
            is LoginState.Processing -> Processing
            is LoginState.Error.Message -> Error(it.error.message, it.isPotentialBlocking)
            is LoginState.Error.SwitchToSrp -> Error(it.error.message)
            is LoginState.Error.SwitchToSso -> Error(it.error.message)
            is LoginState.Error.InvalidPassword -> Error(it.error.message)
            is LoginState.Error.ExternalEmailNotSupported -> ExternalEmailNotSupported
            is LoginState.Error.ExternalSsoNotSupported -> ExternalSsoNotSupported
            is LoginState.Error.UserCheck -> UserCheckError(it.message, it.action)
            is LoginState.Error.UnlockPrimaryKey -> Error(null)
            is LoginState.Error.ChangePassword -> ChangePassword
            is LoginState.NeedAuthSecret -> when (it.authInfo) {
                is AuthInfo.Srp -> NeedSrp(it.authInfo as AuthInfo.Srp)
                is AuthInfo.Sso -> NeedSso(it.authInfo as AuthInfo.Sso)
            }

            is LoginState.LoggedIn -> Success(it.userId)
        }
    }
}
