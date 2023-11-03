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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.signup.ValidateEmail
import me.proton.core.auth.domain.usecase.signup.ValidatePhone
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.telemetry.ProductMetricsDelegateAuth
import me.proton.core.auth.presentation.telemetry.ProductMetricsDelegateAuth.Companion.KEY_METHOD_TYPE
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.telemetry.domain.TelemetryContext
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class RecoveryMethodViewModel @Inject constructor(
    private val validateEmail: ValidateEmail,
    private val validatePhone: ValidatePhone,
    override val telemetryManager: TelemetryManager
) : ProtonViewModel(), TelemetryContext, ProductMetricsDelegateAuth {

    override val productGroup: String = "account.android.signup"
    override val productFlow: String = "mobile_signup_full"

    private val _recoveryMethodUpdate = MutableStateFlow(RecoveryMethodType.EMAIL)
    private val _validationResult = MutableStateFlow<ValidationState>(ValidationState.None)

    val recoveryMethodUpdate = _recoveryMethodUpdate.asStateFlow()
    val validationResult = _validationResult.asStateFlow()

    private var _currentActiveRecoveryMethod: RecoveryMethod = RecoveryMethod(RecoveryMethodType.EMAIL, "")

    val recoveryMethod: RecoveryMethod
        get() = _currentActiveRecoveryMethod

    sealed class ValidationState {
        object None : ValidationState()
        object Processing : ValidationState()
        object Skipped : ValidationState()
        data class Success(val value: Boolean) : ValidationState()
        data class Error(val throwable: Throwable?) : ValidationState()
    }

    /** Called when destination (email or phone) is empty/blank. */
    fun onRecoveryMethodDestinationMissing() {
        _validationResult.tryEmit(ValidationState.Success(false))
    }

    /** Called when destination (email or phone) is empty/blank. */
    fun onRecoveryMethodDestinationSkipped() {
        _validationResult.tryEmit(ValidationState.Skipped)
    }

    /**
     * Sets the currently active verification method that the user chose.
     * If the user changes the verification method tab, the destination is being reset.
     */
    fun setActiveRecoveryMethod(
        userSelectedMethodType: RecoveryMethodType,
        destination: String = ""
    ) {
        _currentActiveRecoveryMethod = RecoveryMethod(userSelectedMethodType, destination)
        _recoveryMethodUpdate.tryEmit(userSelectedMethodType)
    }

    /**
     * Validates the user input recovery destination (email or phone number) on the API.
     */
    fun validateRecoveryDestinationInput() = viewModelScope.launchWithResultContext {
        onResultEnqueueTelemetry("validateEmail") {
            toTelemetryEvent("user.recovery_method.verify", mapOf(KEY_METHOD_TYPE to "email"))
        }
        onResultEnqueueTelemetry("validatePhone") {
            toTelemetryEvent("user.recovery_method.verify", mapOf(KEY_METHOD_TYPE to "sms"))
        }

        flow {
            emit(ValidationState.Processing)
            emit(
                when (_currentActiveRecoveryMethod.type) {
                    RecoveryMethodType.EMAIL -> validateRecoveryEmail()
                    RecoveryMethodType.SMS -> validateRecoveryPhone()
                }.exhaustive
            )
        }.catch { error ->
            emit(ValidationState.Error(error))
        }.onEach {
            _validationResult.tryEmit(it)
        }.collect()
    }

    /**
     * Checks on the API if the email is a valid one.
     */
    private suspend fun validateRecoveryEmail() =
        ValidationState.Success(validateEmail(_currentActiveRecoveryMethod.destination))

    /**
     * Checks on the API if the phone is a valid one.
     */
    private suspend fun validateRecoveryPhone() =
        ValidationState.Success(validatePhone(_currentActiveRecoveryMethod.destination))
}
