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

package me.proton.core.accountrecovery.presentation.compose.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecoveryState
import me.proton.core.accountrecovery.presentation.compose.LogTag
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.hasProtonErrorCode
import me.proton.core.user.domain.entity.UserRecovery.State.Cancelled
import me.proton.core.user.domain.entity.UserRecovery.State.Expired
import me.proton.core.user.domain.entity.UserRecovery.State.Grace
import me.proton.core.user.domain.entity.UserRecovery.State.Insecure
import me.proton.core.user.domain.entity.UserRecovery.State.None
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.catchWhen
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeUserRecoveryState: ObserveUserRecoveryState,
    private val cancelRecovery: CancelRecovery,
    private val keyStoreCrypto: KeyStoreCrypto,
) : ViewModel() {

    private val userId = UserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))

    private val ackFlow = MutableStateFlow(false)
    private val cancellationFlow = MutableStateFlow(CancellationState())

    val initialState = State.Loading

    sealed class State {

        sealed class Opened : State() {
            data class GracePeriodStarted(
                val processing: Boolean = false,
                val passwordError: Boolean = false
            ) : Opened()

            object PasswordChangePeriodStarted : Opened()
            object CancellationHappened : Opened()

            object RecoveryEnded : Opened()
        }

        object Closed : State()
        object Loading : State()

        data class Error(val throwable: Throwable?) : State()
    }

    val state: StateFlow<State> = ackFlow.flatMapLatest { ack ->
        if (ack) flowOf(State.Closed) else observeState()
    }.catchWhen({ it.hasProtonErrorCode(ResponseCodes.PASSWORD_WRONG) }) {
        emit(State.Opened.GracePeriodStarted(passwordError = true))
    }.catch {
        CoreLogger.e(LogTag.ERROR_OBSERVING_STATE, it)
        emit(State.Error(it))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialState
    )

    private fun observeState() = combine(
        observeUserRecoveryState(userId),
        cancellationFlow
    ) { userRecoveryState, cancellationState ->
        when (userRecoveryState) {
            None -> State.Closed
            Grace -> when {
                cancellationState.error != null -> State.Error(cancellationState.error)
                cancellationState.success == true -> State.Closed
                else -> State.Opened.GracePeriodStarted(
                    processing = cancellationState.processing,
                    passwordError = cancellationState.passwordError
                )
            }

            Cancelled -> State.Opened.CancellationHappened
            Insecure -> State.Opened.PasswordChangePeriodStarted
            Expired -> State.Opened.RecoveryEnded
        }
    }

    fun startAccountRecoveryCancel(password: String) = viewModelScope.launch {
        cancellationFlow.update { CancellationState(processing = true) }
        cancellationFlow.value = when {
            password.isEmpty() -> CancellationState(passwordError = true)
            else -> runCatching { cancelRecovery(password.encrypt(keyStoreCrypto), userId) }.fold(
                onSuccess = { CancellationState(success = true) },
                onFailure = { error -> CancellationState(success = false, error = error) }
            )
        }
    }

    fun userAcknowledged() {
        ackFlow.update { true }
    }
}

private data class CancellationState(
    val processing: Boolean = false,
    val success: Boolean? = null,
    val error: Throwable? = null,
    val passwordError: Boolean = false
)
