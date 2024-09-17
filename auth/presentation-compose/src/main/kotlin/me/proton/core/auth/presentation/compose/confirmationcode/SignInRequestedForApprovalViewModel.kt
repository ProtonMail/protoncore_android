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

package me.proton.core.auth.presentation.compose.confirmationcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.sso.ActivateAuthDevice
import me.proton.core.auth.domain.usecase.sso.RejectAuthDevice
import me.proton.core.auth.domain.usecase.sso.ValidateConfirmationCode
import me.proton.core.auth.presentation.compose.confirmationcode.ShareConfirmationCodeWithAdminScreen.getUserId
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject

@HiltViewModel
public class SignInRequestedForApprovalViewModel @Inject constructor(
    private val activateAuthDevice: ActivateAuthDevice,
    private val authDeviceRepository: AuthDeviceRepository,
    private val passphraseRepository: PassphraseRepository,
    private val rejectAuthDevice: RejectAuthDevice,
    private val savedStateHandle: SavedStateHandle,
    private val validateConfirmationCode: ValidateConfirmationCode
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableState: MutableStateFlow<SignInRequestedForApprovalState> =
        MutableStateFlow(SignInRequestedForApprovalState.Idle)

    public val state: StateFlow<SignInRequestedForApprovalState> = mutableState.asStateFlow()

    public fun submit(action: SignInRequestedForApprovalAction): Job = viewModelScope.launch {
        when (action) {
            is SignInRequestedForApprovalAction.Close -> close()
            is SignInRequestedForApprovalAction.Confirm -> confirmRequest()
            is SignInRequestedForApprovalAction.Reject -> rejectRequest()
            is SignInRequestedForApprovalAction.ValidateConfirmationCode -> validateCode(action.confirmationCode)
        }
    }

    private suspend fun validateCode(confirmationCode: String) {
        val newState = when (validateConfirmationCode(userId, confirmationCode)) {
            ValidateConfirmationCode.Result.ConfirmationCodeInputError -> SignInRequestedForApprovalState.Error("")
            ValidateConfirmationCode.Result.ConfirmationCodeInvalid -> SignInRequestedForApprovalState.ConfirmationCodeResult(
                success = false
            )

            ValidateConfirmationCode.Result.ConfirmationCodeValid -> SignInRequestedForApprovalState.ConfirmationCodeResult(
                success = true
            )

            ValidateConfirmationCode.Result.NoDeviceSecret -> SignInRequestedForApprovalState.Error("")
        }
        mutableState.emit(newState)
    }

    private fun confirmRequest() = flow {
        emit(SignInRequestedForApprovalState.Loading)
        val device = requireNotNull(getDevicePendingActivation())
        val passphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        activateAuthDevice(userId, device.deviceId, passphrase)
        emit(SignInRequestedForApprovalState.ConfirmedSuccessfully)
    }.catch { throwable ->
        when (throwable) {
            is IllegalArgumentException -> emit(SignInRequestedForApprovalState.Error(throwable.message))
            else -> throw throwable
        }
    }.launchIn(viewModelScope)

    private fun rejectRequest() = flow {
        emit(SignInRequestedForApprovalState.Loading)
        val device = requireNotNull(getDevicePendingActivation())
        rejectAuthDevice(userId, device.deviceId)
        emit(SignInRequestedForApprovalState.RejectedSuccessfully)
    }.catch { throwable ->
        when (throwable) {
            is IllegalArgumentException -> emit(SignInRequestedForApprovalState.Error(throwable.message))
            else -> throw throwable
        }
    }.launchIn(viewModelScope)

    private suspend fun close() {
        mutableState.emit(SignInRequestedForApprovalState.Close)
    }

    private suspend fun getDevicePendingActivation(): AuthDevice? =
        authDeviceRepository.getByUserId(userId).firstOrNull { it.state == AuthDeviceState.PendingActivation }
}
