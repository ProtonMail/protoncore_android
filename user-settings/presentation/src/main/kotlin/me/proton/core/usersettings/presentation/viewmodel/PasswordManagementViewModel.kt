/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.usersettings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword
import me.proton.core.usersettings.domain.usecase.PerformUpdateUserPassword
import javax.inject.Inject

@HiltViewModel
class PasswordManagementViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val getSettings: GetSettings,
    private val performUpdateLoginPassword: PerformUpdateLoginPassword,
    private val performUpdateUserPassword: PerformUpdateUserPassword
) : ProtonViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)

    val state = _state.asStateFlow()

    private var twoPasswordMode: Boolean? = null
    var secondFactorEnabled: Boolean? = null

    sealed class State {
        object Idle : State()
        data class Mode(val twoPasswordMode: Boolean) : State()
        object UpdatingLoginPassword : State()
        object UpdatingMailboxPassword : State()
        object UpdatingSinglePassModePassword : State()
        sealed class Success : State() {
            data class UpdatingLoginPassword(val settings: UserSettings) : State()
            object UpdatingMailboxPassword : State()
            object UpdatingSinglePassModePassword : State()
        }

        sealed class Error : State() {
            data class General(val error: Throwable) : Error()
            object UpdatingMailboxPassword : Error()
            object UpdatingSinglePassModePassword : Error()
        }
    }

    fun init(userId: UserId) = flow {
        val currentSettings = getSettings(userId)
        twoPasswordMode = currentSettings.password.mode == 2
        secondFactorEnabled = currentSettings.twoFA?.enabled ?: false
        emit(State.Mode(twoPasswordMode!!))
    }.catch { error ->
        _state.tryEmit(State.Error.General(error))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    /**
     * Updates the login password for two password mode users.
     */
    fun updateLoginPassword(
        userId: UserId,
        password: String,
        newPassword: String,
        secondFactorCode: String = ""
    ) = flow {
        if (twoPasswordMode == false) {
            updateMailboxPassword(userId, password, newPassword, secondFactorCode)
            return@flow
        }
        emit(State.UpdatingLoginPassword)
        val encryptedPassword = password.encrypt(keyStoreCrypto)
        val encryptedNewPassword = newPassword.encrypt(keyStoreCrypto)

        val result = performUpdateLoginPassword(
            userId = userId,
            password = encryptedPassword,
            newPassword = encryptedNewPassword,
            secondFactorCode = secondFactorCode
        )
        emit(State.Success.UpdatingLoginPassword(result))
    }.catch { error ->
        _state.tryEmit(State.Error.General(error))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    /**
     * Updates the mailbox password for two password mode users.
     */
    fun updateMailboxPassword(
        userId: UserId,
        loginPassword: String,
        newMailboxPassword: String,
        secondFactorCode: String = ""
    ) = flow {
        emit(if (twoPasswordMode == true) State.UpdatingMailboxPassword else State.UpdatingSinglePassModePassword)
        val encryptedLoginPassword = loginPassword.encrypt(keyStoreCrypto)
        val encryptedNewMailboxPassword = newMailboxPassword.encrypt(keyStoreCrypto)
        val result = performUpdateUserPassword.invoke(
            twoPasswordMode = twoPasswordMode!!,
            userId = userId,
            loginPassword = encryptedLoginPassword,
            newPassword = encryptedNewMailboxPassword,
            secondFactorCode = secondFactorCode
        )
        if (result) {
            emit(
                if (twoPasswordMode == true)
                    State.Success.UpdatingMailboxPassword
                else
                    State.Success.UpdatingSinglePassModePassword
            )
        } else {
            emit(
                if (twoPasswordMode == true)
                    State.Error.UpdatingMailboxPassword
                else
                    State.Error.UpdatingSinglePassModePassword
            )
        }
    }.catch { error ->
        _state.tryEmit(State.Error.General(error))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)
}
