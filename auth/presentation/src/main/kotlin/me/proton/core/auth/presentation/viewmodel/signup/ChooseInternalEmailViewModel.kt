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

package me.proton.core.auth.presentation.viewmodel.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotalV1
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotalV1
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import javax.inject.Inject

@HiltViewModel
internal class ChooseInternalEmailViewModel @Inject constructor(
    private val accountAvailability: AccountAvailability,
) : ProtonViewModel() {

    private val mutableDomains = MutableStateFlow<List<Domain>>(emptyList())
    private val mutableState = MutableStateFlow<State>(State.Idle)

    val state = mutableState.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class Domains(val domains: List<String>) : State()
        data class Success(val username: String, val domain: String) : State()
        sealed class Error : State() {
            object DomainsNotAvailable : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    init {
        fetchDomains()
    }

    private fun fetchDomains() = flow {
        if (mutableDomains.value.isEmpty()) {
            emit(State.Processing)
            mutableDomains.value = accountAvailability.getDomains(metricData = { SignupFetchDomainsTotalV1(it) })
        }
        emit(State.Domains(mutableDomains.value))
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        mutableState.tryEmit(it)
    }.launchIn(viewModelScope)

    fun checkUsername(username: String, domain: String) = flow {
        emit(State.Processing)
        accountAvailability.checkUsername("$username@$domain", metricData = { SignupUsernameAvailabilityTotalV1(it) })
        emit(State.Success(username, domain))
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        mutableState.tryEmit(it)
    }.launchIn(viewModelScope)
}
