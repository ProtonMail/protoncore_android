/*
 * Copyright (c) 2024 Proton AG
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.usecase.sso.ChangeBackupPassword
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeAction.ChangePassword
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeState.Error
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeState.FormError
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeState.Idle
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeState.Loading
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeState.Success
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginSsoChangePasswordTotal
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordChangeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val context: CryptoContext,
    private val changeBackupPassword: ChangeBackupPassword,
    override val observabilityManager: ObservabilityManager
) : ViewModel(), ObservabilityContext {

    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<BackupPasswordChangeAction?>(null)

    public val state: StateFlow<BackupPasswordChangeState> = mutableAction.flatMapLatest { action ->
        when (action) {
            null -> flowOf(Idle)
            is ChangePassword -> onValidatePassword(action)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle)

    public fun submit(action: BackupPasswordChangeAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onValidatePassword(action: ChangePassword) = flow {
        emit(Loading)
        InputValidationResult(
            text = action.backupPassword,
            validationType = ValidationType.PasswordMinLength
        ).onFailure {
            emit(FormError(PasswordFormError.PasswordTooShort))
        }.onSuccess {
            InputValidationResult(
                text = action.backupPassword,
                validationType = ValidationType.PasswordMatch,
                additionalText = action.repeatBackupPassword
            ).onFailure {
                emit(FormError(PasswordFormError.PasswordsDoNotMatch))
            }.onSuccess {
                emitAll(onChangeBackupPassword(action.backupPassword))
            }
        }
    }

    private fun onChangeBackupPassword(backupPassword: String) = flowWithResultContext {
        onCompleteEnqueueObservability { LoginSsoChangePasswordTotal(this) }

        emit(Loading)
        val password = backupPassword.encrypt(context.keyStoreCrypto)
        changeBackupPassword.invoke(userId, password)
        emit(Success)
    }.catchWhen(Throwable::isActionNotAllowed) {
        emit(BackupPasswordChangeState.Close(it.message))
    }.catchAll(LogTag.CHANGE_BACKUP_PASSWORD) {
        emit(Error(it.message))
    }
}
