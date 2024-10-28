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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceSecretString
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
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.SetInput
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Closed
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Confirmed
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Confirming
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Error
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Idle
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Loading
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Rejected
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Rejecting
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DurationFormat
import me.proton.core.util.android.datetime.UtcClock
import javax.inject.Inject

@HiltViewModel
public class MemberApprovalViewModel @Inject constructor(
    private val activateAuthDevice: ActivateAuthDevice,
    private val authDeviceRepository: AuthDeviceRepository,
    private val passphraseRepository: PassphraseRepository,
    private val rejectAuthDevice: RejectAuthDevice,
    private val savedStateHandle: SavedStateHandle,
    private val userManager: UserManager,
    private val validateCode: ValidateConfirmationCode,
    private val durationFormat: DurationFormat,
    private val dateTimeFormat: DateTimeFormat,
    @ApplicationContext private val context: Context,
    @UtcClock private val clock: Clock
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<MemberApprovalAction>(Load())

    public val state: StateFlow<MemberApprovalState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad(action.background)
            is SetInput -> onValidate(action.deviceId, action.code)
            is Confirm -> onConfirm(action.deviceId, action.deviceSecret)
            is Reject -> onReject(action.deviceId)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle(MemberApprovalData()))

    public fun submit(action: MemberApprovalAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun reload(delay: Long = 30000) = viewModelScope.launch {
        delay(delay)
        submit(Load(background = true))
    }

    private suspend fun onLoad(background: Boolean = false) = flow {
        val email = userManager.getUser(userId).getEmail()
        if (!background) {
            emit(Loading(state.value.data.copy(email = email)))
        }
        val devices = getPendingDevices().map { it.toData(context, clock, durationFormat, dateTimeFormat) }
        when {
            devices.isEmpty() -> emit(Closed)
            else -> emit(Idle(state.value.data.copy(email = email, pendingDevices = devices)))
        }
        reload()
    }

    private suspend fun onValidate(deviceId: AuthDeviceId?, code: String?) = flow<MemberApprovalState> {
        when (val result = validateCode.invoke(userId, deviceId, code)) {
            is Result.NoDeviceSecret -> emit(Idle(state.value.data.copy(deviceSecret = null)))
            is Result.Invalid -> emit(Idle(state.value.data.copy(deviceSecret = null)))
            is Result.Valid -> emit(Idle(state.value.data.copy(deviceSecret = result.deviceSecret)))
        }
    }.catch { error ->
        emit(Error(data = state.value.data, error.message))
    }

    private fun onConfirm(deviceId: AuthDeviceId?, deviceSecret: DeviceSecretString?) = flow {
        emit(Confirming(data = state.value.data))
        val passphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        activateAuthDevice.invoke(userId, requireNotNull(deviceId), requireNotNull(deviceSecret), passphrase)
        emit(Confirmed)
    }.catch { error ->
        emit(Error(data = state.value.data, error.message))
    }

    private fun onReject(deviceId: AuthDeviceId) = flow {
        emit(Rejecting(data = state.value.data))
        rejectAuthDevice(userId, deviceId)
        emit(Rejected)
    }.catch { error ->
        emit(Error(data = state.value.data, error.message))
    }

    private suspend fun getPendingDevices(): List<AuthDevice> =
        authDeviceRepository.getByUserId(userId, refresh = true)
            .sortedByDescending { it.createdAtUtcSeconds }
            .filter { it.isPendingMember() || it.isPendingAdmin() }
}
