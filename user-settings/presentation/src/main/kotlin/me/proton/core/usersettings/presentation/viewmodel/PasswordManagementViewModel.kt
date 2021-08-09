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
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword
import javax.inject.Inject

@HiltViewModel
class PasswordManagementViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val getSettings: GetSettings,
    private val userRepository: UserRepository,
    private val performUpdateLoginPassword: PerformUpdateLoginPassword
) : ProtonViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)

    val state = _state.asStateFlow()

    sealed class State {
        object Idle : State()
        data class Mode(val twoPasswordMode: Boolean): State()
        object UpdatingLoginPassword : State()
        object UpdatingMailboxPassword : State()
        data class UpdatingLoginPasswordSuccess(val settings: UserSettings) : State()
        data class UpdatingMailboxPasswordSuccess(val settings: UserSettings) : State()
        sealed class Error : State() {
            data class Message(val message: String?) : Error()
        }
    }

    fun init(userId: UserId) = flow {
        val currentSettings = getSettings(userId)
        emit(State.Mode(currentSettings.password.mode == 2))
    }.catch { error ->
        _state.tryEmit(State.Error.Message(error.message))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    /**
     * Updates the password for single password mode users.
     */
    fun updatePassword() {

    }

    /**
     * Updates the login password for two password mode users.
     */
    fun updateLoginPassword(
        userId: UserId,
        password: String,
        newPassword: String,
        secondFactorCode: String
    ) = flow {
        emit(State.UpdatingLoginPassword)
        val encryptedPassword = password.encryptWith(keyStoreCrypto)
        val encryptedNewPassword = newPassword.encryptWith(keyStoreCrypto)
        val user = userRepository.getUser(userId)
        val username = requireNotNull(user.name ?: user.email)

        val result = performUpdateLoginPassword(
            sessionUserId = userId,
            password = encryptedPassword,
            paid = user.hasSubscription(),
            newPassword = encryptedNewPassword,
            username = username,
            secondFactorCode = secondFactorCode
        )
        emit(State.UpdatingLoginPasswordSuccess(result))
    }.catch { error ->
        _state.tryEmit(State.Error.Message(error.message))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    /**
     * Updates the mailbox password for two password mode users.
     */
    fun updateMailboxPassword(
        userId: UserId,
        loginPassword: String,
        mailboxPassword: String,
        newMailboxPassword: String
    ) = flow {
        emit(State.UpdatingMailboxPassword)
    }.catch { error ->
        _state.tryEmit(State.Error.Message(error.message))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)
}