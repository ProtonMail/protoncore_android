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
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.firstOrDefault
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class ChooseAddressViewModel @ViewModelInject constructor(
    private val usernameDomainAvailability: UsernameDomainAvailability
) : ProtonViewModel(), ViewStateStoreScope {

    val usernameState = ViewStateStore<UsernameState>().lock
    val domainsState = ViewStateStore<DomainState>().lock

    lateinit var domain: String

    sealed class UsernameState {
        object Processing : UsernameState()
        data class Success(val available: Boolean, val username: String) : UsernameState()

        sealed class Error : UsernameState() {
            data class Message(val message: String?) : Error()
        }
    }

    sealed class DomainState {
        object Processing : DomainState()
        data class Success(val domains: List<Domain>) : DomainState()

        sealed class Error : DomainState() {
            data class Message(val message: String?) : Error()
            object NoAvailableDomains : Error()
        }
    }

    init {
        getAvailableDomains()
    }

    private fun getAvailableDomains() = flow {
        emit(DomainState.Processing)
        val domains = usernameDomainAvailability.getDomains()
        domain = domains.firstOrDefault()
        if (domains.isEmpty())
            emit(DomainState.Error.NoAvailableDomains)
        else
            emit(DomainState.Success(domains))
    }.catch { error ->
        domainsState.post(DomainState.Error.Message(error.message))
    }.onEach {
        domainsState.post(it)
    }.launchIn(viewModelScope)

    fun checkUsernameAvailability(username: String) = flow {
        emit(UsernameState.Processing)
        val isAvailable = usernameDomainAvailability.isUsernameAvailable(username)
        emit(UsernameState.Success(isAvailable, username))
    }.catch { error ->
        usernameState.post(UsernameState.Error.Message(error.message))
    }.onEach {
        usernameState.post(it)
    }.launchIn(viewModelScope)
}
