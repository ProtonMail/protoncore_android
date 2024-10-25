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
import me.proton.core.auth.domain.usecase.sso.GenerateConfirmationCode
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.WaitingAdminAction.Load
import me.proton.core.auth.presentation.compose.sso.WaitingAdminState.Error
import me.proton.core.auth.presentation.compose.sso.WaitingAdminState.Loading
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.displayNameNotNull
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.extension.hasTemporaryPassword
import javax.inject.Inject

@HiltViewModel
public class WaitingAdminViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val generateConfirmationCode: GenerateConfirmationCode,
    private val userManager: UserManager
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<WaitingAdminAction>(Load())

    public val state: StateFlow<WaitingAdminState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad()
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Loading)

    public fun submit(action: WaitingAdminAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onLoad() = flow {
        emit(Loading)
        val user = userManager.getUser(userId)
        val confirmationCode = generateConfirmationCode(userId)
        emit(
            WaitingAdminState.DataLoaded(
                username = user.getEmail() ?: user.displayNameNotNull(),
                confirmationCode = confirmationCode,
                canUseBackupPassword = !user.hasTemporaryPassword()
            )
        )
    }.catch {
        emit(Error(it.message))
    }
}
