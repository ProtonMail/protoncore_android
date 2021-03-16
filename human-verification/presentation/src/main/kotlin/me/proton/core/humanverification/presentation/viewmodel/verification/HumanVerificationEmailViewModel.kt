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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult

/**
 * View model class that handles and supports [TokenType.EMAIL] verification method (type) fragment.
 *
 * @author Dino Kadrikj.
 */
internal class HumanVerificationEmailViewModel @ViewModelInject constructor(
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination
) : ProtonViewModel(), HumanVerificationCode {

    private val _validationEmail = getNewValidation()
    private val _verificationCodeStatusEmail = getNewVerificationCodeStatus()

    override val validation: StateFlow<ViewModelResult<List<CountryUIModel>>> = _validationEmail.asStateFlow()
    override val verificationCodeStatus: StateFlow<ViewModelResult<Boolean>> = _verificationCodeStatusEmail.asStateFlow()

    /**
     * Tells the API to send the verification code (token) to the email destination.
     *
     * @param email the email address that the user entered as a destination.
     */
    fun sendVerificationCode(sessionId: SessionId, email: String) {
        viewModelScope.launch {
            if (email.isEmpty()) {
                _validationEmail.tryEmit(
                    ViewModelResult.Error(EmptyDestinationException("Destination email: $email is invalid."))
                )
                return@launch
            }
            sendVerificationCodeToEmail(sessionId, email)
        }
    }

    /**
     * Contacts the API and sends the verification code to the destination email address the user
     * has entered in the UI.
     */
    private suspend fun sendVerificationCodeToEmail(sessionId: SessionId, email: String) {
        val deferred = sendVerificationCodeToEmailDestination.invoke(sessionId, email)
        if (deferred is VerificationResult.Success) {
            _verificationCodeStatusEmail.tryEmit(ViewModelResult.Success(true))
        } else {
            _verificationCodeStatusEmail.tryEmit(ViewModelResult.Error(VerificationCodeSendingException()))
        }
    }
}
