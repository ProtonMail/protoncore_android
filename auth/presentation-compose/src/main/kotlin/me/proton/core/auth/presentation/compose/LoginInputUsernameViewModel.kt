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
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
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
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.ChangePassword
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.Error
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.ExternalNotSupported
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.Idle
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.NeedSrp
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.NeedSso
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.Processing
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.Success
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.UserCheckError
import me.proton.core.auth.presentation.compose.LoginInputUsernameState.ValidationError
import me.proton.core.auth.presentation.compose.LoginRoutes.Arg.getUsername
import me.proton.core.auth.presentation.compose.LoginRoutes.Arg.setUsername
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.telemetry.domain.TelemetryContext
import me.proton.core.telemetry.domain.TelemetryManager
import javax.inject.Inject

@HiltViewModel
public class LoginInputUsernameViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val loginFlow: LoginFlow,
    public override val observabilityManager: ObservabilityManager,
    public override val telemetryManager: TelemetryManager
) : ViewModel(), ObservabilityContext, TelemetryContext {

    private val mutableAction = MutableSharedFlow<LoginInputUsernameAction>()

    public val state: StateFlow<LoginInputUsernameState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is LoginInputUsernameAction.SetUsername -> onValidateUsername(action)
            is LoginInputUsernameAction.SetToken -> onSetToken(action)
        }
    }.stateIn(viewModelScope, WhileSubscribed(0, 0), Idle)

    public fun submit(action: LoginInputUsernameAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onValidateUsername(action: LoginInputUsernameAction.SetUsername) = flow {
        val isValid = InputValidationResult(action.username, ValidationType.Username).isValid
        when {
            !isValid -> emit(ValidationError)
            else -> emitAll(onSetUsername(action))
        }
    }

    private fun onSetUsername(action: LoginInputUsernameAction.SetUsername) = loginFlow.invoke(
        username = action.username.also { savedStateHandle.setUsername(action.username) },
    ).mapToLoginInputUsernameState()

    private fun onSetToken(action: LoginInputUsernameAction.SetToken) = loginFlow.invoke(
        username = requireNotNull(savedStateHandle.getUsername()),
        secret = AuthSecret.Sso(action.token)
    ).mapToLoginInputUsernameState()

    private fun Flow<LoginState>.mapToLoginInputUsernameState() = map {
        when (it) {
            is LoginState.Processing -> Processing
            is LoginState.Error.Message -> Error(it.error.message, it.isPotentialBlocking)
            is LoginState.Error.SwitchToSrp -> Error(it.error.message)
            is LoginState.Error.SwitchToSso -> Error(it.error.message)
            is LoginState.Error.InvalidPassword -> Error(it.error.message)
            is LoginState.Error.ExternalNotSupported -> ExternalNotSupported
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
