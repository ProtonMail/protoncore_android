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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class ChooseAddressViewModel @ViewModelInject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val usernameDomainAvailability: UsernameDomainAvailability
) : ProtonViewModel(), ViewStateStoreScope {

    private val userIdFlow = MutableStateFlow<UserId?>(null)

    val state = ViewStateStore<State>().lock

    sealed class State {
        object Processing : State()
        data class Success(val username: String, val domain: Domain) : State()
        data class Data(val username: String?, val domains: List<Domain>) : State()
        sealed class Error : State() {
            object DomainsNotAvailable : Error()
            object UsernameNotAvailable : Error()
            data class Message(val message: String?) : Error()
        }
    }

    init {
        userIdFlow.asStateFlow().filterNotNull().transformLatest { userId ->
            emit(State.Processing)
            val domains = usernameDomainAvailability.getDomains()
            if (domains.isEmpty()) {
                emit(State.Error.DomainsNotAvailable)
                return@transformLatest
            }
            val user = usernameDomainAvailability.getUser(userId)
            emit(State.Data(user.name, domains))
        }.catch { error ->
            state.post(State.Error.Message(error.message))
        }.onEach {
            state.post(it)
        }.launchIn(viewModelScope)
    }

    fun stopChooseAddressWorkflow(
        userId: UserId
    ): Job = viewModelScope.launch {
        accountWorkflow.handleCreateAddressFailed(userId)
    }

    fun setUserId(userId: UserId) = userIdFlow.tryEmit(userId)

    fun checkUsername(username: String, domain: Domain) = flow {
        emit(State.Processing)
        val userId = checkNotNull(userIdFlow.value)
        if (usernameDomainAvailability.isUsernameAvailable(userId, username)) {
            emit(State.Success(username, domain))
        } else {
            emit(State.Error.UsernameNotAvailable)
        }
    }.catch { error ->
        state.post(State.Error.Message(error.message))
    }.onEach {
        state.post(it)
    }.launchIn(viewModelScope)
}
