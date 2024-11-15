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
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.domain.usecase.sso.ActivateAuthDevice
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputAction.RequestAdminHelp
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputAction.SetNavigationDone
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputAction.Submit
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.Close
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.Error
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.FormError
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.Idle
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.Loading
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.NavigateToRequestAdminHelp
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.NavigateToRoot
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.Success
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.isActionNotAllowed
import me.proton.core.network.domain.isMissingScope
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginSsoActivateDeviceTotal
import me.proton.core.observability.domain.metrics.LoginSsoInputPasswordTotal
import me.proton.core.observability.domain.metrics.LoginSsoInputPasswordTotal.InputPasswordStatus.invalidPassphrase
import me.proton.core.observability.domain.metrics.LoginSsoInputPasswordTotal.InputPasswordStatus.noKeySalt
import me.proton.core.observability.domain.metrics.LoginSsoInputPasswordTotal.InputPasswordStatus.noPrimaryKey
import me.proton.core.observability.domain.metrics.LoginSsoInputPasswordTotal.InputPasswordStatus.unlockSuccess
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val activateAuthDevice: ActivateAuthDevice,
    private val userManager: UserManager,
    private val passphraseRepository: PassphraseRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val authDeviceRepository: AuthDeviceRepository,
    override val observabilityManager: ObservabilityManager,
) : ViewModel(), ObservabilityContext {

    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<BackupPasswordInputAction?>(null)

    public val state: StateFlow<BackupPasswordInputState> = mutableAction.flatMapLatest { action ->
        when (action) {
            null -> flowOf(Idle)
            is Submit -> onBackupPassword(action.backupPassword)
            is RequestAdminHelp -> onRequestAdminHelp()
            is SetNavigationDone -> onSetNavigationDone()
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle)

    public fun submit(action: BackupPasswordInputAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onSetNavigationDone(): Flow<BackupPasswordInputState> = flow {
        emit(Idle)
    }

    private fun onBackupPassword(backupPassword: String): Flow<BackupPasswordInputState> = flow {
        emit(Loading)
        val isValid = InputValidationResult(backupPassword, ValidationType.Password).isValid
        when {
            !isValid -> emit(FormError(R.string.backup_password_input_password_empty))
            else -> emitAll(onUnlockUser(backupPassword))
        }
    }

    private fun onUnlockUser(backupPassword: String): Flow<BackupPasswordInputState> = flowWithResultContext {
        onFailureEnqueueObservability { LoginSsoInputPasswordTotal(this) }

        emit(Loading)
        backupPassword.toByteArray().use { password ->
            when (userManager.unlockWithPassword(userId, password)) {
                UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> {
                    enqueueObservability(LoginSsoInputPasswordTotal(noKeySalt))
                    emit(Error(context.getString(R.string.backup_password_no_key_salts)))
                }
                UserManager.UnlockResult.Error.NoPrimaryKey -> {
                    enqueueObservability(LoginSsoInputPasswordTotal(noPrimaryKey))
                    emit(Error(context.getString(R.string.backup_password_no_primary_key)))
                }
                UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> {
                    enqueueObservability(LoginSsoInputPasswordTotal(invalidPassphrase))
                    emit(Error(context.getString(R.string.backup_password_invalid)))
                }
                UserManager.UnlockResult.Success -> {
                    enqueueObservability(LoginSsoInputPasswordTotal(unlockSuccess))
                    emitAll(onActivateDevice())
                }
            }
        }
    }.catchWhen(Throwable::isActionNotAllowed) {
        emit(Close(it.message))
    }.catchWhen(Throwable::isMissingScope) {
        emit(Close(it.message))
    }.catchAll(LogTag.UNLOCK_USER) {
        emit(Error(it.message))
    }

    private fun onActivateDevice(): Flow<BackupPasswordInputState> = flowWithResultContext {
        onCompleteEnqueueObservability { LoginSsoActivateDeviceTotal(this) }

        emit(Loading)
        // unlockWithPassword set the passphrase.
        val passphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        activateAuthDevice.invoke(userId, passphrase)
        emit(Success)
    }.catchAll(LogTag.ACTIVATE_DEVICE) {
        emit(Error(it.message))
    }

    private fun onRequestAdminHelp(): Flow<BackupPasswordInputState> = flow {
        emit(Loading)
        val deviceId = deviceSecretRepository.getByUserId(userId)?.deviceId
        val state = deviceId?.let { authDeviceRepository.getByDeviceId(userId, it)?.state }
        when {
            state == AuthDeviceState.PendingAdminActivation -> emit(NavigateToRoot)
            else -> emit(NavigateToRequestAdminHelp)
        }
    }
}
