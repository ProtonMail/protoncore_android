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

package me.proton.core.auth.presentation.compose.sso.backuppassword.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.compose.R
import me.proton.core.network.domain.ApiException
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordInputViewModel @Inject constructor() : ViewModel() {
    private val _state: MutableStateFlow<BackupPasswordInputState> =
        MutableStateFlow(BackupPasswordInputState.Idle)

    public val state: StateFlow<BackupPasswordInputState> = _state.asStateFlow()

    public fun submit(action: BackupPasswordInputAction): Job = viewModelScope.launch {
        when (action) {
            is BackupPasswordInputAction.Submit -> onSubmit(action.backupPassword)
        }
    }

    private fun onSubmit(backupPassword: String) = try {
        _state.update { BackupPasswordInputState.Loading }
        check(InputValidationResult(backupPassword, ValidationType.Password).isValid)
        submitBackupPassword(backupPassword)
        _state.update { BackupPasswordInputState.Success }
    } catch (e: ApiException) {
        _state.update { BackupPasswordInputState.Error(e) }
    } catch (ignored: IllegalStateException) {
        _state.update { BackupPasswordInputState.FormError(R.string.backup_password_input_password_empty) }
    }

    private fun submitBackupPassword(backupPassword: String) {
        TODO("submit backupPassword to unlock user; throw error if not successful")
    }
}
