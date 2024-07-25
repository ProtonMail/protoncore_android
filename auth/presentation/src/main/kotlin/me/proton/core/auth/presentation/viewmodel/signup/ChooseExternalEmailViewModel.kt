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

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.parcelize.Parcelize
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupEmailAvailabilityTotal
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.presentation.savedstate.flowState
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class ChooseExternalEmailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountAvailability: AccountAvailability,
    override val observabilityManager: ObservabilityManager,
    savedStateHandle: SavedStateHandle
) : ProtonViewModel(), ObservabilityContext {

    private val mainState by savedStateHandle.flowState(
        mutableSharedFlow = MutableStateFlow<State>(State.Idle),
        coroutineScope = viewModelScope,
        onStateRestored = this::onStateRestored
    )
    val state = mainState.asSharedFlow()

    @Parcelize
    sealed class State : Parcelable {
        @Parcelize
        data object Idle : State()
        @Parcelize
        data class Processing(val email: String) : State()
        @Parcelize
        data class SwitchInternal(val username: String, val domain: String) : State()
        @Parcelize
        data class Success(val email: String) : State()

        @Parcelize
        sealed class Error : State() {
            @Parcelize
            data class Message(val error: String?) : Error()
        }
    }

    fun checkExternalEmail(email: String) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("checkExternalEmailAvailable") { SignupEmailAvailabilityTotal(this) }
        onResultEnqueueObservability("getAvailableDomains") { SignupFetchDomainsTotal(this) }

        flow {
            emit(State.Processing(email))

            // See CP-5335.
            val domains = accountAvailability.getDomains(userId = null)
            val emailSplit = email.split("@")
            val username = emailSplit.getOrNull(0)
            val domain = emailSplit.getOrNull(1)

            when {
                username != null && domain != null && domain in domains -> {
                    emit(State.SwitchInternal(username, domain))
                }

                else -> {
                    accountAvailability.checkExternalEmail(email = email)
                    emit(State.Success(email))
                }
            }
        }.catch { error ->
            emit(State.Error.Message(error.getUserMessage(context.resources)))
        }.collect {
            mainState.tryEmit(it)
        }
    }

    private fun onStateRestored(state: State) {
        // Resume the process, if it was interrupted:
        if (state is State.Processing) {
            checkExternalEmail(state.email)
        }
    }
}
