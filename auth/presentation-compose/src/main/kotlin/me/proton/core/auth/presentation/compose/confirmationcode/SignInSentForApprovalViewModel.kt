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
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.sso.GenerateConfirmationCode
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.confirmationcode.SignInSentForApprovalAction.Close
import me.proton.core.auth.presentation.compose.confirmationcode.SignInSentForApprovalAction.Load
import me.proton.core.auth.presentation.compose.sso.device.AvailableDeviceUIModel
import me.proton.core.auth.presentation.compose.sso.device.ClientType
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.util.android.datetime.DateTimeFormat
import javax.inject.Inject

@HiltViewModel
public class SignInSentForApprovalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authDeviceRepository: AuthDeviceRepository,
    private val generateConfirmationCode: GenerateConfirmationCode,
    private val dateTimeFormat: DateTimeFormat,
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<SignInSentForApprovalAction>(Load)

    public val state: StateFlow<SignInSentForApprovalState> =
        mutableAction.flatMapLatest { action ->
            when (action) {
                is Close -> onClose()
                is Load -> onLoad()
            }
        }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), SignInSentForApprovalState.Loading)

    public fun submit(action: SignInSentForApprovalAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private suspend fun onClose() = flow {
        emit(SignInSentForApprovalState.Close)
    }

    private fun onLoad() = flow {
        emit(SignInSentForApprovalState.Loading)
        val confirmationCode = generateConfirmationCode.invoke(userId)
        val devices = authDeviceRepository.getByUserId(userId, refresh = true)
        val availableDevices = devices.filter { it.state == AuthDeviceState.Active }
        val uiModels = availableDevices.map {
            AvailableDeviceUIModel(
                id = it.deviceId.id,
                authDeviceName = it.name,
                localizedClientName = it.localizedClientName,
                lastActivityTime = it.lastActivityAtUtcSeconds,
                lastActivityReadable = dateTimeFormat.format(
                    epochSeconds = it.lastActivityAtUtcSeconds,
                    style = DateTimeFormat.DateTimeForm.MEDIUM_DATE
                ),
                clientType = ClientType.Android // TODO: Add ClientType -> Platform
            )
        }
        emit(SignInSentForApprovalState.DataLoaded(confirmationCode, uiModels))
    }
}
