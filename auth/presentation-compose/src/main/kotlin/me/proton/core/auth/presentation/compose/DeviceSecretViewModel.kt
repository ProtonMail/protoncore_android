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

import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.sso.AssociateAuthDevice
import me.proton.core.auth.domain.usecase.sso.CheckOtherDevices
import me.proton.core.auth.domain.usecase.PostLoginSsoAccountSetup
import me.proton.core.auth.domain.usecase.sso.ActivateAuthDevice
import me.proton.core.auth.domain.usecase.sso.CreateAuthDevice
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.Arg.getUserId
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.ChangePassword
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Close
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.DeviceRejected
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Error
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.FirstLogin
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.InvalidSecret
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Loading
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Success
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject
import me.proton.core.auth.domain.usecase.sso.AssociateAuthDevice.Result as AssociateResult
import me.proton.core.auth.domain.usecase.sso.CheckOtherDevices.Result as DevicesResult
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result as SetupResult

@HiltViewModel
public class DeviceSecretViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountWorkflow: AccountWorkflowHandler,
    private val userManager: UserManager,
    private val passphraseRepository: PassphraseRepository,
    private val createAuthDevice: CreateAuthDevice,
    private val checkOtherDevices: CheckOtherDevices,
    private val associateAuthDevice: AssociateAuthDevice,
    private val activateAuthDevice: ActivateAuthDevice,
    private val postLoginSsoAccountSetup: PostLoginSsoAccountSetup,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val authDeviceRepository: AuthDeviceRepository,
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val mutableAction = MutableStateFlow<DeviceSecretAction>(DeviceSecretAction.Load())

    public val state: StateFlow<DeviceSecretViewState> = mutableAction.flatMapLatest { action ->
        when (action) {
            is DeviceSecretAction.Close -> onClose()
            is DeviceSecretAction.Load -> onLoad(action.background)
        }
    }.stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis), Loading(null))

    public fun submit(action: DeviceSecretAction): Job = viewModelScope.launch {
        mutableAction.emit(action)
    }

    private fun onClose(): Flow<DeviceSecretViewState> = flow {
        accountWorkflow.handleDeviceSecretFailed(userId)
        emit(Close(email = state.value.email))
    }

    private fun observeAuthDevice(): Flow<DeviceSecretViewState> =
        deviceSecretRepository.observeByUserId(userId)
            .filterNotNull().mapLatest { it.deviceId }
            .flatMapLatest { deviceId ->
                authDeviceRepository.observeByDeviceId(userId, deviceId)
                    .filterNotNull()
                    .flatMapLatest { device ->
                        when (device.state) {
                            AuthDeviceState.Inactive -> reload()
                            AuthDeviceState.PendingActivation -> reload()
                            AuthDeviceState.PendingAdminActivation -> reload()
                            AuthDeviceState.NoSession -> onDeviceActivated()
                            AuthDeviceState.Active -> onDeviceActivated()
                            AuthDeviceState.Rejected -> onDeviceRejected()
                        }
                    }
            }

    private fun reload(delay: Long = 10000): Flow<DeviceSecretViewState> = flow {
        delay(delay)
        emitAll(onLoad(background = true))
    }

    private fun onLoad(background: Boolean = false): Flow<DeviceSecretViewState> = flow {
        if (!background) { emit(Loading(userManager.getUser(userId).email)) }
        emitAll(
            when (val deviceSecret = deviceSecretRepository.getByUserId(userId)) {
                null -> onCreateDevice()
                else -> when (val result = associateAuthDevice.invoke(
                    userId = userId,
                    deviceId = deviceSecret.deviceId,
                    deviceToken = deviceSecret.token,
                )) {
                    is AssociateResult.Error.DeviceNotFound -> onCreateDevice()
                    is AssociateResult.Error.DeviceTokenInvalid -> onCreateDevice()
                    is AssociateResult.Error.SessionAlreadyAssociated -> onCreateDevice()
                    is AssociateResult.Error.DeviceNotActive -> onDeviceNotActive()
                    is AssociateResult.Error.DeviceRejected -> onDeviceRejected()
                    is AssociateResult.Success -> onDeviceAssociated(result.encryptedSecret)
                }
            }
        )
    }.catch {
        emit(Error(email = state.value.email, it.message))
    }

    private fun onCreateDevice() = flow {
        emit(Loading(email = state.value.email))
        createAuthDevice.invoke(userId, Build.MODEL)
        emitAll(onLoad())
    }

    private fun onDeviceNotActive() = flow {
        val user = userManager.getUser(userId)
        when {
            user.keys.isEmpty() -> emitAll(onFirstLogin())
            else -> emitAll(onCheckOtherDevices())
        }
    }

    private fun onFirstLogin() = flow {
        emit(FirstLogin(email = state.value.email))
        emitAll(observeAuthDevice())
    }

    private fun onCheckOtherDevices() = flow {
        when (checkOtherDevices.invoke(userId)) {
            is DevicesResult.OtherDevicesAvailable -> {
                emit(InvalidSecret.OtherDevice.WaitingMember(email = state.value.email))
                emitAll(observeAuthDevice())
            }

            is DevicesResult.AdminHelpRequested -> {
                emit(InvalidSecret.NoDevice.WaitingAdmin(email = state.value.email))
                emitAll(observeAuthDevice())
            }

            is DevicesResult.AdminHelpRequired -> {
                emit(InvalidSecret.NoDevice.RequireAdmin(email = state.value.email))
                emitAll(observeAuthDevice())
            }

            is DevicesResult.BackupPassword -> {
                emitAll(onLoginWithBackup())
            }
        }
    }

    private fun onLoginWithBackup() = flow {
        when (passphraseRepository.getPassphrase(userId)) {
            null -> {
                emit(InvalidSecret.NoDevice.BackupPassword(email = state.value.email))
                emitAll(observeAuthDevice())
            }

            else -> emitAll(onDeviceActivated())
        }
    }

    private fun onDeviceRejected(): Flow<DeviceSecretViewState> = flow {
        emit(DeviceRejected(email = state.value.email))
    }

    private fun onDeviceAssociated(encryptedSecret: Based64Encoded) = flow {
        emit(Loading(email = state.value.email))
        activateAuthDevice.invoke(userId, encryptedSecret)
        emitAll(onDeviceActivated())
    }

    private fun onDeviceActivated(): Flow<DeviceSecretViewState> = flow {
        emit(Loading(email = state.value.email))
        when (val result = postLoginSsoAccountSetup.invoke(userId)) {
            is SetupResult.AccountReady -> emit(Success(email = state.value.email, userId))
            is SetupResult.Error.UnlockPrimaryKeyError -> emit(Error(email = state.value.email, null))
            is SetupResult.Error.UserCheckError -> emit(Error(email = state.value.email, result.error.localizedMessage))
            is SetupResult.Need -> when (result) {
                is PostLoginAccountSetup.Result.Need.ChangePassword -> emit(ChangePassword(email = state.value.email))
                else -> error("Unexpected state for Global SSO user.")
            }
        }
    }
}
