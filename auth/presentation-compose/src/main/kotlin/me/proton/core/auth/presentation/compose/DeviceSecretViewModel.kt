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

package me.proton.core.auth.presentation.compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.presentation.compose.DeviceSecretScreen.getUserId
import javax.inject.Inject

@HiltViewModel
public class DeviceSecretViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountWorkflow: AccountWorkflowHandler,
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableState = MutableStateFlow<DeviceSecretViewState>(DeviceSecretViewState.Idle)
    public val state: StateFlow<DeviceSecretViewState> = mutableState.asStateFlow()

    public fun submit(action: DeviceSecretAction): Job = viewModelScope.launch {
        when (action) {
            is DeviceSecretAction.Close -> onClose()
        }
    }

    private suspend fun onClose() {
        accountWorkflow.handleAccountDisabled(userId)
        mutableState.emit(DeviceSecretViewState.Close)
    }
}
