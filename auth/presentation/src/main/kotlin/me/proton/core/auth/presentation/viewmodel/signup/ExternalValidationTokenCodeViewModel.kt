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
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.usecase.CheckCreationTokenValidity
import me.proton.core.humanverification.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.user.domain.entity.createUserType
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
        checkCreationTokenValidity(
            token = destinationToken,
            tokenType = TokenType.EMAIL.value,
            type = type.createUserType()
        )
        emit(ValidationState.Success(destinationToken))
    }.catch { error ->
        emit(ValidationState.Error.Message(error.message))
    }.onEach {
        _validationState.tryEmit(it)
    }.launchIn(viewModelScope)

    /**
     * This function resends another token to the same destination.
     */
    fun resendCode(destination: String) = flow {
        emit(ViewModelResult.Processing)
        resendVerificationCodeToDestination(
            tokenType = TokenType.EMAIL,
            destination = destination
        )
        emit(ViewModelResult.Success(true))
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _verificationCodeResendState.tryEmit(it)
    }.launchIn(viewModelScope)
}
