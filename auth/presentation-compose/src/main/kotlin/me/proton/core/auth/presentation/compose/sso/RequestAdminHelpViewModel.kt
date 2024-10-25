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
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.presentation.compose.DeviceApprovalRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpAction.Load
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpAction.Submit
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpState.AdminHelpHelpRequested
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpState.Error
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpState.Idle
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpState.Loading
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import me.proton.core.util.kotlin.catchAll
import javax.inject.Inject

@HiltViewModel
public class RequestAdminHelpViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val organizationRepository: OrganizationRepository
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<RequestAdminHelpAction>(Load())

    public val state: StateFlow<RequestAdminHelpState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad()
            is Submit -> onSubmit()
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Loading(RequestAdminHelpData()))

    public fun submit(action: RequestAdminHelpAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onLoad() = flow {
        var data = state.value.data
        emit(Loading(data))

        val signature = requireNotNull(organizationRepository.getOrganizationSignature(userId))
        data = data.copy(organizationAdminEmail = signature.fingerprintSignatureAddress)
        emit(Idle(data))

        val settings = organizationRepository.getOrganizationSettings(userId)
        val logo = settings.logoId?.let { organizationRepository.getOrganizationLogo(userId, it) }

        data = data.copy(organizationIcon = logo)
        emit(Idle(data))
    }.catchAll(LogTag.ORGANIZATION_LOAD) { error ->
        emit(Error(state.value.data, error))
    }

    private fun onSubmit() = flow {
        emit(Loading(state.value.data))
        val deviceId = requireNotNull(deviceSecretRepository.getByUserId(userId)?.deviceId)
        val device = authDeviceRepository.getByDeviceId(userId, deviceId)
        check(device?.state != AuthDeviceState.PendingAdminActivation) {
            "Device is already pending admin activation."
        }
        authDeviceRepository.requestAdminHelp(userId, deviceId)
        emit(AdminHelpHelpRequested(state.value.data))
    }.catch { error ->
        emit(Error(state.value.data, error))
    }
}
