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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.country.domain.usecase.MostUsedCountryCode
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToPhoneDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import javax.inject.Inject

/**
 * View model class that handles and supports [TokenType.SMS] verification method (type) fragment.
 */
@HiltViewModel
internal class HumanVerificationSMSViewModel @Inject constructor(
    private val mostUseCountryCode: MostUsedCountryCode,
    private val sendVerificationCodeToPhoneDestination: SendVerificationCodeToPhoneDestination
) : ProtonViewModel(), HumanVerificationCode {

    private val _mostUsedCallingCode = MutableStateFlow<ViewModelResult<Int>>(ViewModelResult.None)
    private val _validationSMS = getNewValidation()
    private val _verificationCodeStatusSMS = getNewVerificationCodeStatus()

    val mostUsedCallingCode = _mostUsedCallingCode.asStateFlow()
    override val validation = _validationSMS.asStateFlow()
    override val verificationCodeStatus = _verificationCodeStatusSMS.asStateFlow()

    /**
     * Tells the API to send the verification code (token) to the phone number destination.
     *
     * @param countryCallingCode the calling code part of the phone number
     * @param phoneNumber the phone number that the user entered (without the calling code part)
     */
    fun sendVerificationCodeToDestination(sessionId: SessionId?, countryCallingCode: String, phoneNumber: String) =
        viewModelScope.launch {
            runCatching {
                require(phoneNumber.isNotEmpty()) { "Destination phone number: $phoneNumber is invalid." }
            }.onSuccess {
                _validationSMS.tryEmit(ViewModelResult.Processing)
                sendVerificationCodeToSMS(sessionId, countryCallingCode + phoneNumber)
            }.onFailure {
                _validationSMS.tryEmit(ViewModelResult.Error(it))
            }
        }

    /**
     * Tries to return the most used country calling code and later to display it as a suggestion
     * in the SMS verification UI.
     */
    fun getMostUsedCallingCode() = flow {
        emit(ViewModelResult.Processing)
        val code = mostUseCountryCode()
        code?.let {
            emit(ViewModelResult.Success(it))
        } ?: run {
            emit(ViewModelResult.Error(null))
        }
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _mostUsedCallingCode.tryEmit(it)
    }.launchIn(viewModelScope)

    /**
     * Contacts the API and sends the verification code to the destination phone number the user
     * has entered in the UI.
     */
    private suspend fun sendVerificationCodeToSMS(sessionId: SessionId?, phoneNumber: String) {
        runCatching {
            _verificationCodeStatusSMS.tryEmit(ViewModelResult.Processing)
            sendVerificationCodeToPhoneDestination.invoke(sessionId, phoneNumber)
        }.onSuccess {
            _verificationCodeStatusSMS.tryEmit(ViewModelResult.Success(phoneNumber))
        }.onFailure {
            _verificationCodeStatusSMS.tryEmit(ViewModelResult.Error(it))
        }
    }
}
