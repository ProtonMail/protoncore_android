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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.isActive
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.sso.GenerateConfirmationCode
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.WaitingMemberAction.Load
import me.proton.core.auth.presentation.compose.sso.WaitingMemberState.DataLoaded
import me.proton.core.auth.presentation.compose.sso.WaitingMemberState.Error
import me.proton.core.auth.presentation.compose.sso.WaitingMemberState.Loading
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DurationFormat
import me.proton.core.util.android.datetime.UtcClock
import javax.inject.Inject

@HiltViewModel
public class WaitingMemberViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authDeviceRepository: AuthDeviceRepository,
    private val generateConfirmationCode: GenerateConfirmationCode,
    private val durationFormat: DurationFormat,
    @UtcClock private val clock: Clock
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<WaitingMemberAction>(Load())

    public val state: StateFlow<WaitingMemberState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad(action.background)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Loading)

    public fun submit(action: WaitingMemberAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun reload(delay: Long = 30000) = viewModelScope.launch {
        delay(delay)
        submit(Load(background = true))
    }

    private fun onLoad(background: Boolean = false) = flow {
        if (!background) {
            emit(Loading)
        }
        val confirmationCode = generateConfirmationCode.invoke(userId)
        val devices = authDeviceRepository.getByUserId(userId, refresh = true)
        val availableDevices = devices.filter { it.isActive() }
        val uiModels = availableDevices.map { it.toData(clock, durationFormat) }
        emit(DataLoaded(confirmationCode, uiModels))
        reload()
    }.catch {
        emit(Error(it.message))
    }
}
