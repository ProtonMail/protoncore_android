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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.usecase.sso.ActivateAuthDevice
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.util.kotlin.catchAll
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val activateAuthDevice: ActivateAuthDevice,
    private val userManager: UserManager,
    private val passphraseRepository: PassphraseRepository,
) : ViewModel() {

    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<BackupPasswordInputAction?>(null)

    public val state: StateFlow<BackupPasswordInputState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is BackupPasswordInputAction.Submit -> onBackupPassword(action.backupPassword)
            null -> flowOf(BackupPasswordInputState.Idle)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), BackupPasswordInputState.Idle)

    public fun submit(action: BackupPasswordInputAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onBackupPassword(backupPassword: String): Flow<BackupPasswordInputState> = flow {
        emit(BackupPasswordInputState.Loading)
        val isValid = InputValidationResult(backupPassword, ValidationType.Password).isValid
        when {
            !isValid -> emit(BackupPasswordInputState.FormError(R.string.backup_password_input_password_empty))
            else -> emitAll(onUnlockUser(backupPassword))
        }
    }

    private fun onUnlockUser(backupPassword: String): Flow<BackupPasswordInputState> = flow {
        emit(BackupPasswordInputState.Loading)
        backupPassword.toByteArray().use { password ->
            when (userManager.unlockWithPassword(userId, password)) {
                UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> error(context.getString(R.string.backup_password_no_key_salts))
                UserManager.UnlockResult.Error.NoPrimaryKey -> error(context.getString(R.string.backup_password_no_primary_key))
                UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> error(context.getString(R.string.backup_password_invalid))
                UserManager.UnlockResult.Success -> emitAll(onActivateDevice())
            }
        }
    }.catchAll(LogTag.UNLOCK_USER) {
        emit(BackupPasswordInputState.Error(it.message))
    }

    private fun onActivateDevice(): Flow<BackupPasswordInputState> = flow {
        emit(BackupPasswordInputState.Loading)
        // unlockWithPassword set the passphrase.
        val passphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        activateAuthDevice.invoke(userId, passphrase)
        emit(BackupPasswordInputState.Success)
    }.catchAll(LogTag.ACTIVATE_DEVICE) {
        emit(BackupPasswordInputState.Error(it.message))
    }
}
