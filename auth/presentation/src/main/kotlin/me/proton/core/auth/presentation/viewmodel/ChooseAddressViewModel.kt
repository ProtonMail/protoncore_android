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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import javax.inject.Inject

@HiltViewModel
class ChooseAddressViewModel @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val accountAvailability: AccountAvailability
) : ProtonViewModel() {

    private val userIdFlow = MutableStateFlow<UserId?>(null)

    private val _state = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    val state = _state.asSharedFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class Success(val username: String, val domain: Domain) : State()
        data class Data(val username: String?, val domains: List<Domain>) : State()
        sealed class Error : State() {
            object DomainsNotAvailable : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    init {
        userIdFlow.asStateFlow().filterNotNull().transformLatest { userId ->
            emit(State.Processing)
            val domains = accountAvailability.getDomains()
            if (domains.isEmpty()) {
                emit(State.Error.DomainsNotAvailable)
                return@transformLatest
            }
            val user = accountAvailability.getUser(userId)
            emit(State.Data(user.name, domains))
        }.catch { error ->
            emit(State.Error.Message(error))
        }.onEach {
            _state.tryEmit(it)
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
        accountAvailability.checkUsername(userId, username)
        emit(State.Success(username, domain))
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)
}
