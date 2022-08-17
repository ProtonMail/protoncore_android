/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.viewmodel.hv2.method

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.usecase.CheckCreationTokenValidity
import me.proton.core.humanverification.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import javax.inject.Inject

/**
 * View model class that handles the input of the verification code (token) previously sent to any
 * destination [TokenType.EMAIL] or [TokenType.SMS].
 * It will contact the API in order to verify that the entered code is the correct one.
 */
@HiltViewModel
class HumanVerificationEnterCodeViewModel @Inject constructor(
    private val resendVerificationCodeToDestination: ResendVerificationCodeToDestination,
    private val checkCreationTokenValidity: CheckCreationTokenValidity
) : ProtonViewModel() {

    private val _verificationCodeResendState = MutableSharedFlow<ViewModelResult<Boolean>>(
        replay = 1,
        extraBufferCapacity = 3
    )
    private val _validationState = MutableSharedFlow<ViewModelResult<String>>(
        replay = 1,
        extraBufferCapacity = 3
    )

    val verificationCodeResendState = _verificationCodeResendState.asSharedFlow()
    val validationState = _validationState.asSharedFlow()

    fun getToken(destination: String?, code: String) = "$destination:$code"

    fun validateToken(
        sessionId: SessionId?,
        token: String,
        tokenType: TokenType
    ) = flow {
        emit(ViewModelResult.Processing)
        checkCreationTokenValidity.invoke(
            sessionId = sessionId,
            token = token,
            tokenType = tokenType
        )
        emit(ViewModelResult.Success(token))
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _validationState.tryEmit(it)
    }.launchIn(viewModelScope)

    fun resendCode(
        sessionId: SessionId?,
        destination: String,
        tokenType: TokenType
    ) = flow {
        emit(ViewModelResult.Processing)
        resendVerificationCodeToDestination.invoke(
            sessionId = sessionId,
            destination = destination,
            tokenType = tokenType
        )
        emit(ViewModelResult.Success(true))
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _verificationCodeResendState.tryEmit(it)
    }.launchIn(viewModelScope)
}
