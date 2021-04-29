/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.user.domain.entity.VerificationResult
import me.proton.core.user.domain.usecase.ResendVerificationCodeToDestination

/**
 * View model class that handles the input of the verification code (token) previously sent to any
 * destination [UserVerificationTokenType.EMAIL] or [UserVerificationTokenType.SMS].
 * It will contact the API in order to verify that the entered code is the correct one.
 *
 * @author Dino Kadrikj.
 */
class HumanVerificationEnterCodeViewModel @ViewModelInject constructor(
    private val resendVerificationCodeToDestination: ResendVerificationCodeToDestination
) : ProtonViewModel() {

    private val _verificationCodeResendStatus = MutableStateFlow<ViewModelResult<Boolean>>(ViewModelResult.None)

    lateinit var tokenType: UserVerificationTokenType

    // a special case is when the destination is absent because the user has the code from
    // somewhere else (ex. from customer support)
    var destination: String? = null

    val verificationCodeResendStatus = _verificationCodeResendStatus.asStateFlow()

    /**
     * This function resends another token to the same previously set destination.
     */
    fun resendCode(sessionId: SessionId?) {
        destination?.let {
            viewModelScope.launch {
                val result = resendVerificationCodeToDestination(sessionId, tokenType, it)
                if (result is VerificationResult.Success) {
                    _verificationCodeResendStatus.tryEmit(ViewModelResult.Success(true))
                } else {
                    _verificationCodeResendStatus.tryEmit(ViewModelResult.Error(VerificationCodeSendingException()))
                }
            }
        }
    }
}
