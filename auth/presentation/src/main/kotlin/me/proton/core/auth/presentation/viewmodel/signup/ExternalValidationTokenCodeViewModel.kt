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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.createUserType
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.user.domain.entity.VerificationResult
import me.proton.core.user.domain.usecase.CheckCreationTokenValidity
import me.proton.core.user.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ExternalValidationTokenCodeViewModel @Inject constructor(
    private val resendVerificationCodeToDestination: ResendVerificationCodeToDestination,
    private val checkCreationTokenValidity: CheckCreationTokenValidity
) : ProtonViewModel() {

    private val _verificationCodeResendState = MutableStateFlow<ViewModelResult<Boolean>>(ViewModelResult.None)
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Idle)

    val verificationCodeResendState = _verificationCodeResendState.asStateFlow()
    val validationState = _validationState.asStateFlow()

    sealed class ValidationState {
        object Idle : ValidationState()
        object Processing : ValidationState()
        data class Success(val token: String) : ValidationState()
        sealed class Error : ValidationState() {
            data class Message(val message: String?) : ValidationState()
        }
    }

    fun validateToken(destination: String, token: String, type: AccountType) = flow {
        emit(ValidationState.Processing)
        val destinationToken = "$destination:$token"
        val result = checkCreationTokenValidity(
            token = destinationToken,
            tokenType = UserVerificationTokenType.EMAIL.tokenTypeValue,
            type = type.createUserType()
        )
        if (result is VerificationResult.Error) {
            emit(ValidationState.Error.Message(result.message))
        } else {
            emit(ValidationState.Success(destinationToken))
        }
    }.catch { error ->
        _validationState.tryEmit(ValidationState.Error.Message(error.message))
    }.onEach {
        _validationState.tryEmit(it)
    }.launchIn(viewModelScope)

    /**
     * This function resends another token to the same destination.
     */
    fun resendCode(destination: String) = flow {
        emit(ViewModelResult.Processing)
        val result =
            resendVerificationCodeToDestination(
                tokenType = UserVerificationTokenType.EMAIL,
                destination = destination
            )
        when (result) {
            is VerificationResult.Error -> _verificationCodeResendState.tryEmit(
                ViewModelResult.Error(VerificationCodeSendingException(result.message))
            )
            is VerificationResult.Success -> _verificationCodeResendState.tryEmit(ViewModelResult.Success(true))
        }.exhaustive
    }.catch { error ->
        _verificationCodeResendState.tryEmit(ViewModelResult.Error(error))
    }.onEach {
        _verificationCodeResendState.tryEmit(it)
    }.launchIn(viewModelScope)
}
