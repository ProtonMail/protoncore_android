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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveAccountRecoveryState
import me.proton.core.accountrecovery.presentation.compose.LogTag
import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogType
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class AccountRecoveryViewModel @Inject constructor(
    private val observeAccountRecoveryState: ObserveAccountRecoveryState,
    private val cancelRecovery: CancelRecovery,
    private val keyStoreCrypto: KeyStoreCrypto,
) : ViewModel() {

    private val userIdFlow: MutableStateFlow<UserId?> = MutableStateFlow(null)
    private val ackFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val gracePeriodCancellationFlow: MutableStateFlow<GracePeriodCancellationState> =
        MutableStateFlow(GracePeriodCancellationState())

    val initialState = State.Loading

    sealed class State {

        sealed class Opened : State() {
            data class GracePeriodStarted(
                val processing: Boolean = false
            ) : Opened()

            object PasswordChangePeriodStarted : Opened()
            object CancellationHappened : Opened()

            object RecoveryEnded : Opened()
        }

        object Closed : State()
        object Loading : State()

        data class Error(val message: String?) : State()
    }

    fun setUser(userId: UserId) {
        userIdFlow.update { userId }
    }

    val state: StateFlow<State> = userIdFlow.flatMapLatest { userId ->
        if (userId == null) {
            return@flatMapLatest flowOf(State.Loading)
        }

        combine(
            observeAccountRecoveryState(userId = userId, refresh = true),
            ackFlow,
            gracePeriodCancellationFlow
        ) { accountRecoveryState, acknowledged, gracePeriodCancellationProcessing ->
            if (acknowledged) {
                State.Closed
            } else {
                val dialogType = accountRecoveryState.toDialogType()
                if (dialogType == null) State.Closed
                else when (dialogType) {
                    AccountRecoveryDialogType.GRACE_PERIOD_STARTED -> {
                        when {
                            gracePeriodCancellationProcessing.error != null -> State.Error(
                                gracePeriodCancellationProcessing.error.message
                            )

                            gracePeriodCancellationProcessing.success == true -> State.Closed
                            else -> State.Opened.GracePeriodStarted(
                                processing = gracePeriodCancellationProcessing.processing
                            )
                        }
                    }

                    AccountRecoveryDialogType.CANCELLATION_HAPPENED -> State.Opened.CancellationHappened
                    AccountRecoveryDialogType.PASSWORD_CHANGE_PERIOD_STARTED ->
                        State.Opened.PasswordChangePeriodStarted

                    AccountRecoveryDialogType.RECOVERY_WINDOW_ENDED -> State.Opened.RecoveryEnded
                }
            }
        }
    }.catch {
        CoreLogger.e(LogTag.ERROR_OBSERVING_USER, it)
        emit(State.Error(it.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialState
    )

    fun startAccountRecoveryCancel() = viewModelScope.launch {
        val password: String by lazy { TODO("Obtain password from the input field.") }
        userIdFlow.first().let { userId ->
            if (userId != null) {
                gracePeriodCancellationFlow.update { GracePeriodCancellationState(processing = true) }
                runCatching {
                    cancelRecovery(password.encrypt(keyStoreCrypto), userId)
                }.fold(
                    onSuccess = {
                        gracePeriodCancellationFlow.update { GracePeriodCancellationState(success = true) }
                    },
                    onFailure = { error ->
                        gracePeriodCancellationFlow.update {
                            GracePeriodCancellationState(
                                success = false,
                                error = error
                            )
                        }
                    }
                )
            } else {
                gracePeriodCancellationFlow.update {
                    GracePeriodCancellationState(success = false)
                }
            }
        }
    }

    fun userAcknowledged() {
        ackFlow.update { true }
    }
}

private data class GracePeriodCancellationState(
    val processing: Boolean = false,
    val success: Boolean? = null,
    val error: Throwable? = null
)

fun AccountRecoveryState.toDialogType(): AccountRecoveryDialogType? {
    return when (this) {
        AccountRecoveryState.None -> null
        AccountRecoveryState.GracePeriod -> AccountRecoveryDialogType.GRACE_PERIOD_STARTED
        AccountRecoveryState.ResetPassword -> AccountRecoveryDialogType.PASSWORD_CHANGE_PERIOD_STARTED
        AccountRecoveryState.Cancelled -> AccountRecoveryDialogType.CANCELLATION_HAPPENED
        AccountRecoveryState.Expired -> AccountRecoveryDialogType.RECOVERY_WINDOW_ENDED
    }.exhaustive
}
