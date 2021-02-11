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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupUsername
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class CreateAddressViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val setupUsername: SetupUsername,
    private val setupOriginalAddress: SetupOriginalAddress
) : ProtonViewModel(), ViewStateStoreScope {

    val upgradeState = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        object Success : State()
        sealed class Error : State() {
            data class Message(val message: String?) : Error()
        }
    }

    fun upgradeAccount(
        userId: UserId,
        username: String,
        domain: String
    ) = flow {
        emit(State.Processing)

        setupUsername.invoke(userId, username)
        setupOriginalAddress.invoke(userId, domain)

        accountWorkflow.handleCreateAddressSuccess(userId)
        accountWorkflow.handleAccountReady(userId)

        emit(State.Success)
    }.catch { error ->
        upgradeState.post(State.Error.Message(error.message))
    }.onEach {
        upgradeState.post(it)
    }.launchIn(viewModelScope)
}
