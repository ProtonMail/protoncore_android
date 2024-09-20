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

package me.proton.core.auth.presentation.compose.sso.admin

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
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.presentation.compose.DeviceApprovalRoutes.Arg.getUserId
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.usecase.GetOrganization
import me.proton.core.util.kotlin.catchAll
import javax.inject.Inject

@HiltViewModel
public class AskAdminAccessViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val getOrganization: GetOrganization
) : ViewModel() {
    private val userId by lazy { savedStateHandle.getUserId() }
    private val mutableAction = MutableStateFlow<AskAdminAction>(AskAdminAction.Load)

    public val state: StateFlow<AskAdminState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is AskAdminAction.Load -> onLoad(userId)
            is AskAdminAction.Close -> onClose()
            is AskAdminAction.Submit -> onSubmit()
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), AskAdminState.Loading)

    public fun submit(action: AskAdminAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onClose() = flow<AskAdminState> {
        emit(AskAdminState.Close)
    }

    private fun onSubmit() = flow<AskAdminState> {
        emit(AskAdminState.Loading)
        val deviceId = requireNotNull(deviceSecretRepository.getByUserId(userId)?.deviceId)
        authDeviceRepository.pingAdminForHelp(userId, deviceId)
    }.catch { error ->
        emit(AskAdminState.Error(error))
    }

    private fun onLoad(userId: UserId) = flow {
        emit(AskAdminState.Loading)
        val organization = requireNotNull(getOrganization(userId, false)) {
            "Organization not found for user ${userId}."
        }
        emit(
            AskAdminState.DataLoaded(
                organizationAdminEmail = organization.email, // TODO get from BE
                organizationIcon = null, // TODO get from BE /api/core/{_version}/organizations/logo/{enc_id}
            )
        )
    }.catchAll(LogTag.ORGANIZATION_LOAD) { error ->
        emit(AskAdminState.Error(error))
    }
}
