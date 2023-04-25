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
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotal
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class ChooseUsernameViewModel @Inject constructor(
    private val accountAvailability: AccountAvailability,
) : ProtonViewModel() {

    private val mainState = MutableStateFlow<State>(State.Idle)
    val state = mainState.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class Success(val username: String) : State()
        sealed class Error : State() {
            data class Message(val error: Throwable) : Error()
        }
    }

    fun checkUsername(username: String) = flow {
        emit(State.Processing)
        // See CP-5335.
        accountAvailability.getDomains(
            userId = null,
            metricData = { SignupFetchDomainsTotal(it.toHttpApiStatus()) }
        )
        accountAvailability.checkUsernameUnauthenticated(
            username = username,
            metricData = { SignupUsernameAvailabilityTotal(it.toHttpApiStatus()) }
        )
        emit(State.Success(username))
    }.catch { error ->
        emit(State.Error.Message(error))
    }.onEach {
        mainState.tryEmit(it)
    }.launchIn(viewModelScope)
}
