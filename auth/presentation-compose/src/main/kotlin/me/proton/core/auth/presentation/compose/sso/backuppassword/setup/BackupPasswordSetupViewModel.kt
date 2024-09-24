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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.sso.CreateAuthDevice
import me.proton.core.auth.domain.usecase.sso.GenerateDeviceSecret
import me.proton.core.auth.domain.usecase.sso.VerifyUnprivatization
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupAction.Load
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupAction.SetPassword
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupState.Error
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupState.FormError
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupState.Idle
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupState.Loading
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupState.Success
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val context: CryptoContext,
    private val generateDeviceSecret: GenerateDeviceSecret,
    private val verifyUnprivatization: VerifyUnprivatization,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val createAuthDevice: CreateAuthDevice,
    private val organizationRepository: OrganizationRepository,
) : ViewModel() {

    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<BackupPasswordSetupAction>(Load)

    public val state: StateFlow<BackupPasswordSetupState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is Load -> onLoad()
            is SetPassword -> onSetPassword(action)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Idle(BackupPasswordSetupData()))

    public fun submit(action: BackupPasswordSetupAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onLoad() = flow {
        emit(Loading(state.value.data))
        val organization = organizationRepository.getOrganization(userId)
        val settings = organizationRepository.getOrganizationSettings(userId)
        val logo = settings.logoId?.let { id -> organizationRepository.getOrganizationLogo(userId, id) }
        val data = BackupPasswordSetupData(
            organizationIcon = logo,
            organizationName = organization.displayName,
        )
        emit(Idle(data))
        emitAll(onVerifyUnprivatization())
    }.catch {
        emit(Error(state.value.data, it.message))
    }

    private fun onVerifyUnprivatization() = flow {
        emit(Loading(state.value.data))
        when (val result = verifyUnprivatization.invoke(userId)) {
            is VerifyUnprivatization.Result.UnprivatizeUserSuccess -> {
                val data = state.value.data.copy(
                    organizationAdminEmail = result.adminEmail,
                    organizationPublicKey = result.organizationPublicKey
                )
                emit(Idle(data))
            }

            is VerifyUnprivatization.Result.PublicAddressKeysError,
            is VerifyUnprivatization.Result.UnprivatizeStateError,
            is VerifyUnprivatization.Result.VerificationError -> {
                emit(Error(state.value.data, null))
            }
        }
    }.catch {
        emit(Error(state.value.data, it.message))
    }

    private fun onSetPassword(action: SetPassword) = flow {
        emit(Loading(state.value.data))
        InputValidationResult(
            text = action.backupPassword,
            validationType = ValidationType.PasswordMinLength
        ).onFailure {
            emit(FormError(state.value.data, BackupPasswordSetupFormError.PasswordTooShort))
        }.onSuccess {
            InputValidationResult(
                text = action.backupPassword,
                validationType = ValidationType.PasswordMatch,
                additionalText = action.repeatBackupPassword
            ).onFailure {
                emit(FormError(state.value.data, BackupPasswordSetupFormError.PasswordsDoNotMatch))
            }.onSuccess {
                when (val organizationPublicKey = state.value.data.organizationPublicKey) {
                    null -> emit(Error(state.value.data, null))
                    else -> emitAll(onSetupPrimaryKeys(action.backupPassword, organizationPublicKey))
                }
            }
        }
    }

    private fun onSetupPrimaryKeys(
        backupPassword: String,
        organizationPublicKey: Armored
    ) = flow {
        emit(Loading(state.value.data))
        val password = backupPassword.encrypt(context.keyStoreCrypto)
        val deviceSecret = generateDeviceSecret.invoke()
        setupPrimaryKeys.invoke(
            userId = userId,
            password = password,
            accountType = AccountType.External,
            internalDomain = null,
            organizationPublicKey = organizationPublicKey,
            deviceSecret = deviceSecret
        )
        createAuthDevice.invoke(
            userId = userId,
            deviceName = Build.MODEL,
            deviceSecret = deviceSecret
        )
        emit(Success(state.value.data))
    }.catch {
        emit(Error(state.value.data, it.message))
    }
}
