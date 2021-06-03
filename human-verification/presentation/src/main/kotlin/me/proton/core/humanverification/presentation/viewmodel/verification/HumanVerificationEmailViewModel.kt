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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import javax.inject.Inject

/**
 * View model class that handles and supports [TokenType.EMAIL] verification method (type) fragment.
 *
 * @author Dino Kadrikj.
 */
@HiltViewModel
internal class HumanVerificationEmailViewModel @Inject constructor(
    private val sendVerificationCodeToEmailDestination: SendVerificationCodeToEmailDestination
) : ProtonViewModel(), HumanVerificationCode {

    private val _verificationCodeStatusEmail = getNewVerificationCodeStatus()

    override val verificationCodeStatus = _verificationCodeStatusEmail

    /**
     * Tells the API to send the verification code (token) to the email destination.
     *
     * @param email the email address that the user entered as a destination.
     */
    fun sendVerificationCode(sessionId: SessionId?, email: String) = flow {
        emit(ViewModelResult.Processing)
        sendVerificationCodeToEmailDestination.invoke(sessionId, email)
        emit(ViewModelResult.Success(email))
    }.catch { error ->
        _verificationCodeStatusEmail.tryEmit(ViewModelResult.Error(error))
    }.onEach {
        _verificationCodeStatusEmail.tryEmit(it)
    }.launchIn(viewModelScope)
}
