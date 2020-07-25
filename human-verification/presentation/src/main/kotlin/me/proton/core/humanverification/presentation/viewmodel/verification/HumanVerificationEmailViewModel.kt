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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import studio.forface.viewstatestore.LockedViewStateStore
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStore

/**
 * View model class that handles and supports [TokenType.EMAIL] verification method (type) fragment.
 *
 * @author Dino Kadrikj.
 */
internal class HumanVerificationEmailViewModel @ViewModelInject constructor(
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination
) : ProtonViewModel(), HumanVerificationCode {

    private val validationEmail = ViewStateStore<List<CountryUIModel>>(ViewState.Loading).lock
    private val verificationCodeStatusEmail = ViewStateStore<Boolean>().lock

    override val validation: LockedViewStateStore<List<CountryUIModel>>
        get() = validationEmail

    override val verificationCodeStatus: LockedViewStateStore<Boolean>
        get() = verificationCodeStatusEmail

    /**
     * Tells the API to send the verification code (token) to the email destination.
     *
     * @param email the email address that the user entered as a destination.
     */
    fun sendVerificationCode(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (email.isEmpty()) {
                validation.postError(EmptyDestinationException("Destination email: $email is invalid."))
                return@launch
            }
            sendVerificationCodeToEmail(email)
        }
    }

    /**
     * Contacts the API and sends the verification code to the destination email address the user
     * has entered in the UI.
     */
    private suspend fun sendVerificationCodeToEmail(email: String) {
        val deferred = sendVerificationCodeToEmailDestination.invoke(email)
        if (deferred is VerificationResult.Success) {
            verificationCodeStatus.post(true)
        } else {
            verificationCodeStatus.postError(VerificationCodeSendingException())
        }
    }
}
