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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.compose.LoginTwoStepViewState.UsernameInput
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.network.domain.ApiException
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.telemetry.domain.TelemetryContext
import me.proton.core.telemetry.domain.TelemetryManager
import javax.inject.Inject

@HiltViewModel
public class LoginTwoStepViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountWorkflow: AccountWorkflowHandler,
    private val createLoginSession: CreateLoginSession,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    public override val observabilityManager: ObservabilityManager,
    public override val telemetryManager: TelemetryManager
) : ViewModel(), ObservabilityContext, TelemetryContext {

    //override val productGroup: String = "account.any.signup"
    //override val productFlow: String = "mobile_signup_full"

    private val mutableState = MutableStateFlow<LoginTwoStepViewState>(UsernameInput.Idle)
    public val state: StateFlow<LoginTwoStepViewState> = mutableState.asStateFlow()

    public fun submit(action: LoginTwoStepAction): Job = viewModelScope.launch {
        when (action) {
            is LoginTwoStepAction.SetUsername -> onSetUsername(action.username)
            is LoginTwoStepAction.SetPassword -> onSetPassword(action.password)
            is LoginTwoStepAction.Close -> onClose()
        }
    }

    private fun onSetUsername(username: String) = try {
        TODO("Not yet implemented")
    } catch (e: IllegalStateException) {

    } catch (e: ApiException) {

    }

    private fun onSetPassword(password: String) = try {
        TODO("Not yet implemented")
    } catch (e: IllegalStateException) {

    } catch (e: ApiException) {

    }

    private fun onClose() {
        TODO("Not yet implemented")
    }
}