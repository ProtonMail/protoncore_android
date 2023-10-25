/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotal
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class ChooseInternalEmailViewModel @Inject constructor(
    private val accountAvailability: AccountAvailability,
    override val observabilityManager: ObservabilityManager
) : ProtonViewModel(), ObservabilityContext {

    private var preFillUsername: String? = null
    private var preFillDomain: String? = null
    private val domainsState = MutableStateFlow<List<Domain>>(emptyList())
    private val mainState = MutableStateFlow<State>(State.Idle)

    val state = mainState.asStateFlow()

    sealed class State {
        object Idle : State()

        object Processing : State()
        data class Ready(
            val username: String? = null,
            val domain: String? = null,
            val domains: List<String>,
        ) : State()

        data class Success(val username: String, val domain: String) : State()
        sealed class Error : State() {
            data class DomainsNotAvailable(val error: Throwable) : Error()
            data class Message(val error: Throwable) : Error()
        }
    }

    init {
        getDomains()
    }

    fun getDomains() = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("getAvailableDomains") { SignupFetchDomainsTotal(this) }

        flow {
            emit(State.Processing)
            // See CP-5335.
            domainsState.value = accountAvailability.getDomains(userId = null)
            emit(State.Ready(username = preFillUsername, domain = preFillDomain, domains = domainsState.value))
        }.catch { error ->
            emit(State.Error.DomainsNotAvailable(error))
        }.collect {
            mainState.tryEmit(it)
        }
    }

    fun preFill(username: String?, domain: String?) = flow {
        emit(State.Processing)
        preFillUsername = username
        preFillDomain = domain
        emit(
            State.Ready(
                username = preFillUsername,
                domain = preFillDomain,
                domains = domainsState.value
            )
        )
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        mainState.tryEmit(it)
    }.launchIn(viewModelScope)

    fun checkUsername(username: String, domain: String) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("checkUsernameAvailable") { SignupUsernameAvailabilityTotal(this) }

        flow {
            emit(State.Processing)
            accountAvailability.checkUsernameUnauthenticated("$username@$domain")
            emit(State.Success(username, domain))
        }.catch { error ->
            emit(State.Error.Message(error))
        }.onEach {
            mainState.tryEmit(it)
        }.collect()
    }
}
