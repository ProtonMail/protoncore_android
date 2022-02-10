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
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateRecoveryEmail
import javax.inject.Inject

@HiltViewModel
class UpdateRecoveryEmailViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val getUserSettings: GetUserSettings,
    private val userRepository: UserRepository,
    private val performUpdateRecoveryEmail: PerformUpdateRecoveryEmail
) : ProtonViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)

    val state = _state.asStateFlow()

    var secondFactorEnabled: Boolean? = null

    sealed class State {
        object Idle : State()
        object LoadingCurrent : State()
        object UpdatingCurrent : State()
        data class LoadingSuccess(val recoveryEmail: String?) : State()
        data class UpdatingSuccess(val recoveryEmail: String?) : State()
        data class Error(val error: Throwable) : State()
    }

    /**
     * Returns the current recovery email address.
     */
    fun getCurrentRecoveryAddress(userId: UserId) = flow {
        emit(State.LoadingCurrent)
        val currentSettings = getUserSettings(userId, refresh = true)
        secondFactorEnabled = currentSettings.twoFA?.enabled ?: false
        emit(State.LoadingSuccess(currentSettings.email?.value))
    }.catch { error ->
        _state.tryEmit(State.Error(error))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    /**
     * Updates (replaces) the user's recovery email address.
     */
    fun updateRecoveryEmail(
        userId: UserId,
        newRecoveryEmail: String,
        password: String,
        secondFactorCode: String
    ) = flow {
        emit(State.UpdatingCurrent)
        val encryptedPassword = password.encrypt(keyStoreCrypto)
        val user = userRepository.getUser(userId)
        val username = requireNotNull(user.name ?: user.email)

        val updateRecoveryEmailResult = performUpdateRecoveryEmail(
            sessionUserId = userId,
            newRecoveryEmail = newRecoveryEmail,
            username = username,
            password = encryptedPassword,
            secondFactorCode = secondFactorCode
        )
        emit(State.UpdatingSuccess(updateRecoveryEmailResult.email?.value))
    }.catch { error ->
        _state.tryEmit(State.Error(error))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)
}
