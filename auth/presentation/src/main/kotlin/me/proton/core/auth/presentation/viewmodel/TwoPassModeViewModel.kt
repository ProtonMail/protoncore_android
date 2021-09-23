/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@HiltViewModel
class TwoPassModeViewModel @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val keyStoreCrypto: KeyStoreCrypto
) : ProtonViewModel() {

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val state = _state.asSharedFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        sealed class Success : State() {
            data class UserUnLocked(val userId: UserId) : Success()
        }

        sealed class Error : State() {
            data class CannotUnlockPrimaryKey(val error: UserManager.UnlockResult.Error) : Error()
            data class Message(val message: String?) : Error()
        }
    }

    fun stopMailboxLoginFlow(
        userId: UserId
    ): Job = viewModelScope.launch { accountWorkflow.handleTwoPassModeFailed(userId) }

    fun tryUnlockUser(
        userId: UserId,
        password: String
    ) = flow {
        emit(State.Processing)

        val encryptedPassword = password.encrypt(keyStoreCrypto)
        val state = unlockUserPrimaryKey(userId, encryptedPassword)

        emit(state)
    }.catch { error ->
        emit(State.Error.Message(error.message))
    }.onEach { state ->
        _state.tryEmit(state)
    }.launchIn(viewModelScope)

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        password: EncryptedString
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result == UserManager.UnlockResult.Success) {
            accountWorkflow.handleTwoPassModeSuccess(userId)
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked(userId)
        } else {
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }
}
