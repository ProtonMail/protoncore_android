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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class TwoPassModeViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey
) : ProtonViewModel(), ViewStateStoreScope {

    val mailboxLoginState = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        sealed class Success : State() {
            object UserUnLocked : Success()
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
        password: ByteArray
    ) = flow {
        emit(State.Processing)

        val state = unlockUserPrimaryKey(userId, password)

        emit(state)
    }.catch { error ->
        mailboxLoginState.post(State.Error.Message(error.message))
    }.onEach { state ->
        mailboxLoginState.post(state)
    }.launchIn(viewModelScope)

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        password: ByteArray
    ): State {
        val result = unlockUserPrimaryKey.invoke(userId, password)
        return if (result == UserManager.UnlockResult.Success) {
            accountWorkflow.handleTwoPassModeSuccess(userId)
            accountWorkflow.handleAccountReady(userId)
            State.Success.UserUnLocked
        } else {
            accountWorkflow.handleTwoPassModeFailed(userId)
            State.Error.CannotUnlockPrimaryKey(result as UserManager.UnlockResult.Error)
        }
    }
}
