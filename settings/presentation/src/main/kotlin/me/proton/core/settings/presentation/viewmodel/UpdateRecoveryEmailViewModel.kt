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

package me.proton.core.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.settings.domain.usecase.GetSettings
import me.proton.core.settings.domain.usecase.PerformUpdateRecoveryEmail
import javax.inject.Inject

@HiltViewModel
class UpdateRecoveryEmailViewModel @Inject constructor(
    private val getSettings: GetSettings,
    private val performUpdateRecoveryEmail: PerformUpdateRecoveryEmail
) : ProtonViewModel() {

    private val _currentRecoveryEmailState = MutableStateFlow<CurrentRecoveryEmailState>(CurrentRecoveryEmailState.Idle)
    private val _updateRecoveryEmailState = MutableStateFlow<UpdateRecoveryEmailState>(UpdateRecoveryEmailState.Idle)

    val currentRecoveryEmailState = _currentRecoveryEmailState.asStateFlow()
    val updateRecoveryEmailState = _updateRecoveryEmailState.asStateFlow()

    sealed class CurrentRecoveryEmailState {
        object Idle : CurrentRecoveryEmailState()
        object Processing : CurrentRecoveryEmailState()
        data class Success(val recoveryEmail: String?) : CurrentRecoveryEmailState()
        sealed class Error : CurrentRecoveryEmailState() {
            data class Message(val message: String?) : CurrentRecoveryEmailState.Error()
        }
    }

    sealed class UpdateRecoveryEmailState {
        object Idle : UpdateRecoveryEmailState()
        object Processing : UpdateRecoveryEmailState()
        data class Success(val recoveryEmail: String) : UpdateRecoveryEmailState()
        sealed class Error : UpdateRecoveryEmailState() {
            data class Message(val message: String?) : UpdateRecoveryEmailState.Error()
        }
    }

    /**
     * Returns the current recovery email address.
     */
    fun getCurrentRecoveryAddress(userId: UserId) = flow {
        emit(CurrentRecoveryEmailState.Processing)
        val currentSettings = getSettings(userId)
        emit(CurrentRecoveryEmailState.Success(currentSettings.email?.value))
    }.catch { error ->
        _currentRecoveryEmailState.tryEmit(CurrentRecoveryEmailState.Error.Message(error.message))
    }.onEach { plans ->
        _currentRecoveryEmailState.tryEmit(plans)
    }.launchIn(viewModelScope)

    /**
     * Updates (replaces) the user's recovery email address.
     */
    fun updateRecoveryEmail(
        userId: UserId,
        newRecoveryEmail: String,
        username: String,
        password: EncryptedString,
        twoFactorCode: String
    ) = flow {
        emit(UpdateRecoveryEmailState.Processing)
        val updateRecoveryEmailResult = performUpdateRecoveryEmail(
            sessionUserId = userId,
            newRecoveryEmail = newRecoveryEmail,
            username = username,
            password = password,
            twoFactorCode = twoFactorCode
        )
        // we expect always value for the email on success, thus !!
        emit(UpdateRecoveryEmailState.Success(updateRecoveryEmailResult.email!!.value))
    }.catch { error ->
        _updateRecoveryEmailState.tryEmit(UpdateRecoveryEmailState.Error.Message(error.message))
    }.onEach { plans ->
        _updateRecoveryEmailState.tryEmit(plans)
    }.launchIn(viewModelScope)
}
