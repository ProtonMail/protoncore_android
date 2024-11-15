/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.usecase.CreateLoginLessSession
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginLessAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.isCredentialLessDisabled
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class CredentialLessViewModel @Inject constructor(
    private val createLoginSession: CreateLoginLessSession,
    private val postLoginAccountSetup: PostLoginLessAccountSetup,
) : ProtonViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Idle)

    val initialState = State.Idle

    val state: StateFlow<State> = mutableState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = initialState
    )

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class AccountSetupResult(
            val userId: UserId,
            val result: PostLoginAccountSetup.UserCheckResult
        ) : State()

        data class CredentialLessDisabled(val error: Throwable) : State()

        data class Error(val error: Throwable) : State()
    }

    fun startLoginLessWorkflow() = viewModelScope.launchWithResultContext {
        flow {
            emit(State.Processing)

            val sessionInfo = createLoginSession()
            val result = postLoginAccountSetup(userId = sessionInfo.userId)

            emit(State.AccountSetupResult(sessionInfo.userId, result))
        }.catchWhen(Throwable::isCredentialLessDisabled) {
            emit(State.CredentialLessDisabled(it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it))
        }.collect { state ->
            mutableState.tryEmit(state)
        }
    }
}

