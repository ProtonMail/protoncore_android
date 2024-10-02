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

package me.proton.core.auth.presentation.compose.sso

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.isPendingAdmin
import me.proton.core.auth.domain.entity.isPendingMember
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.sso.ActivateAuthDevice
import me.proton.core.auth.domain.usecase.sso.RejectAuthDevice
import me.proton.core.auth.domain.usecase.sso.ValidateConfirmationCode
import me.proton.core.auth.domain.usecase.sso.ValidateConfirmationCode.Result
import me.proton.core.auth.presentation.compose.DeviceApprovalRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.Confirm
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.Load
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.Reject
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.ValidateCode
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.*
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject

@HiltViewModel
public class MemberApprovalViewModel @Inject constructor(
    private val activateAuthDevice: ActivateAuthDevice,
    private val authDeviceRepository: AuthDeviceRepository,
    private val passphraseRepository: PassphraseRepository,
    private val rejectAuthDevice: RejectAuthDevice,
    private val savedStateHandle: SavedStateHandle,
    private val userManager: UserManager,
    private val validateCode: ValidateConfirmationCode
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<MemberApprovalAction>(Load())

    public val state: StateFlow<MemberApprovalState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad()
            is Confirm -> onConfirm()
            is Reject -> onReject()
            is ValidateCode -> onValidateCode(action.code)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle())

    public fun submit(action: MemberApprovalAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private suspend fun onLoad() = flow {
        emit(Loading(userManager.getUser(userId).getDisplayName()))
        when (getPendingDeviceId()) {
            null -> emit(Closed)
            else -> emit(Idle(state.value.email))
        }
    }

    private suspend fun onValidateCode(code: String) = flow {
        val deviceId = requireNotNull(getPendingDeviceId())
        val newState = when (val result = validateCode.invoke(userId, deviceId, code)) {
            is Result.NoDeviceSecret -> Idle(state.value.email)
            is Result.Invalid -> Idle(state.value.email)
            is Result.Valid -> Valid(state.value.email, result.deviceSecret)
        }
        emit(newState)
    }.catch { error ->
        emit(Error(email = state.value.email, error.message))
    }

    private fun onConfirm() = flow {
        val deviceId = requireNotNull(getPendingDeviceId())
        emit(Confirming(email = state.value.email))
        val state = state.value
        check(state is Valid)
        val deviceSecret = state.deviceSecret
        val passphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        activateAuthDevice.invoke(userId, deviceId, deviceSecret, passphrase)
        emit(Confirmed)
    }.catch { error ->
        emit(Error(email = state.value.email, error.message))
    }

    private fun onReject() = flow {
        val deviceId = requireNotNull(getPendingDeviceId())
        emit(Rejecting(email = state.value.email))
        rejectAuthDevice(userId, deviceId)
        emit(Rejected)
    }.catch { error ->
        emit(Error(email = state.value.email, error.message))
    }

    private suspend fun getPendingDeviceId(): AuthDeviceId? =
        // TODO: Add multi PendingActivation support (+UI to pick it, +refresh periodic).
        authDeviceRepository.getByUserId(userId, refresh = true)
            .sortedBy { it.createdAtUtcSeconds }
            .lastOrNull { it.isPendingMember() || it.isPendingAdmin() }?.deviceId
}
