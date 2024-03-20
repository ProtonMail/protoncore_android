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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecoverySelfInitiated
import me.proton.core.accountrecovery.presentation.compose.LogTag
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryDialogViewModel.State
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ResponseCodes.PASSWORD_WRONG
import me.proton.core.network.domain.hasProtonErrorCode
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.AccountRecoveryCancellationTotal
import me.proton.core.observability.domain.metrics.AccountRecoveryScreenViewTotal
import me.proton.core.observability.domain.metrics.AccountRecoveryScreenViewTotal.ScreenId
import me.proton.core.presentation.utils.StringBox
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.user.domain.entity.UserRecovery.State.Cancelled
import me.proton.core.user.domain.entity.UserRecovery.State.Expired
import me.proton.core.user.domain.entity.UserRecovery.State.Grace
import me.proton.core.user.domain.entity.UserRecovery.State.Insecure
import me.proton.core.user.domain.entity.UserRecovery.State.None
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DateTimeFormat.DateTimeForm
import me.proton.core.util.android.datetime.UtcClock
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AccountRecoveryDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @UtcClock private val clock: Clock,
    private val dateTimeFormat: DateTimeFormat,
    private val observeUser: ObserveUser,
    private val observeUserRecovery: ObserveUserRecovery,
    private val observeSelfInitiated: ObserveUserRecoverySelfInitiated,
    private val cancelRecovery: CancelRecovery,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val isAccountRecoveryResetEnabled: IsAccountRecoveryResetEnabled,
    override val observabilityManager: ObservabilityManager
) : ViewModel(), ObservabilityContext {

    private val userId = UserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))
    private fun User.getEmail(): String = email ?: name ?: displayName ?: ""

    private val ackFlow = MutableStateFlow(false)
    private val cancellationFlow = MutableStateFlow(CancellationState())
    private val shouldShowCancellationForm = MutableStateFlow(false)
    private val shouldShowRecoveryReset = MutableStateFlow(false)
    private val selfInitiated = observeSelfInitiated(userId).stateIn(viewModelScope, Eagerly, null)

    val initialState = State.Loading

    val state: StateFlow<State> = ackFlow.flatMapLatest { ack ->
        shouldShowRecoveryReset.flatMapLatest { showRecovery ->
            when {
                ack -> flowOf(State.Closed())
                showRecovery -> flowOf(State.StartPasswordManager(userId))
                else -> observeState()
            }
        }
    }.catch {
        CoreLogger.e(LogTag.ERROR_OBSERVING_STATE, it)
        emit(State.Error(it))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialState
    )

    val screenId: StateFlow<ScreenId?> =
        state.map(State::toScreenId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = null
        )

    internal fun onScreenView(screenId: ScreenId) {
        observabilityManager.enqueue(AccountRecoveryScreenViewTotal(screenId))
    }

    internal fun userAcknowledged() {
        ackFlow.update { true }
    }

    private fun observeState() = combine(
        observeUser(userId).filterNotNull(),
        observeUserRecovery(userId),
        cancellationFlow,
        shouldShowCancellationForm,
        selfInitiated.filterNotNull()
    ) { user, userRecovery, cancellationState, showCancellationForm, selfInitiated ->
        when (userRecovery?.state?.enum) {
            null, None -> State.Closed()
            Grace -> onGracePeriod(cancellationState, showCancellationForm, user, userRecovery)
            Cancelled -> State.Opened.CancellationHappened
            Insecure -> onInsecurePeriod(cancellationState, showCancellationForm, userRecovery, selfInitiated)
            Expired -> State.Opened.RecoveryEnded(email = user.getEmail())
        }
    }

    private fun onGracePeriod(
        cancellationState: CancellationState,
        showCancellationForm: Boolean,
        user: User,
        userRecovery: UserRecovery
    ): State = when (showCancellationForm) {
        true -> cancellationState.toViewModelState(
            onCancelPasswordRequest = { startAccountRecoveryCancel(it) },
            onBack = { hideCancellationForm() }
        )

        false -> State.Opened.GracePeriodStarted(
            email = user.getEmail(),
            remainingHours = (userRecovery.endTime - clock.currentEpochSeconds()).seconds.toHoursCeil().toInt(),
            onShowCancellationForm = { showCancellationForm() }
        )
    }

    private fun onInsecurePeriod(
        cancellationState: CancellationState,
        showCancellationForm: Boolean,
        userRecovery: UserRecovery,
        selfInitiated: Boolean
    ): State = when (showCancellationForm) {
        true -> cancellationState.toViewModelState(
            onCancelPasswordRequest = { startAccountRecoveryCancel(it) },
            onBack = { hideCancellationForm() }
        )

        false -> {
            if (selfInitiated && isAccountRecoveryResetEnabled(userId)) {
                State.Opened.PasswordChangePeriodStarted.SelfInitiated(
                    endDate = dateTimeFormat.format(userRecovery.endTime, DateTimeForm.MEDIUM_DATE),
                    onShowPasswordChangeForm = { startAccountRecoveryReset() },
                    onShowCancellationForm = { showCancellationForm() }
                )
            } else {
                State.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
                    endDate = dateTimeFormat.format(userRecovery.endTime, DateTimeForm.MEDIUM_DATE),
                    onShowCancellationForm = { showCancellationForm() }
                )
            }
        }
    }

    @VisibleForTesting
    internal fun showCancellationForm() {
        shouldShowCancellationForm.update { true }
    }

    private fun hideCancellationForm() {
        shouldShowCancellationForm.update { false }
    }

    private fun startAccountRecoveryReset() {
        shouldShowRecoveryReset.update { true }
    }

    @VisibleForTesting
    internal fun startAccountRecoveryCancel(password: String) =
        viewModelScope.launchWithResultContext {
            onResultEnqueueObservability("account_recovery.cancellation") { AccountRecoveryCancellationTotal(this) }

            cancellationFlow.update { CancellationState(processing = true) }
            cancellationFlow.value = when {
                password.isEmpty() -> CancellationState(passwordError = StringBox(R.string.presentation_field_required))
                else -> runCatching {
                    cancelRecovery(
                        password.encrypt(keyStoreCrypto),
                        userId
                    )
                }.fold(
                    onSuccess = { CancellationState(success = true) },
                    onFailure = { error -> CancellationState(success = false, error = error) }
                )
            }
        }

    sealed class State {

        sealed class Opened : State() {
            data class GracePeriodStarted(
                val email: String,
                val remainingHours: Int,
                val onShowCancellationForm: () -> Unit = {}
            ) : Opened()

            sealed class PasswordChangePeriodStarted : Opened() {
                data class OtherDeviceInitiated(
                    val endDate: String, // formatted day, e.g. "16 Aug"
                    val onShowCancellationForm: () -> Unit = {}
                ) : PasswordChangePeriodStarted()

                data class SelfInitiated(
                    val endDate: String, // formatted day, e.g. "16 Aug"
                    val onShowPasswordChangeForm: () -> Unit = {},
                    val onShowCancellationForm: () -> Unit = {}
                ) : PasswordChangePeriodStarted()
            }

            data class CancelPasswordReset(
                val processing: Boolean = false,
                val passwordError: StringBox? = null,
                val onCancelPasswordRequest: (String) -> Unit = {},
                val onBack: () -> Unit = {}
            ) : Opened()

            object CancellationHappened : Opened()

            data class RecoveryEnded(val email: String) : Opened()
        }

        data class Closed(val hasCancelledSuccessfully: Boolean = false) : State()

        data class StartPasswordManager(val userId: UserId) : State()

        object Loading : State()

        data class Error(val throwable: Throwable?) : State()
    }
}

private data class CancellationState(
    val processing: Boolean = false,
    val success: Boolean? = null,
    val error: Throwable? = null,
    val passwordError: StringBox? = null
) {
    fun toViewModelState(
        onCancelPasswordRequest: (String) -> Unit,
        onBack: () -> Unit
    ): State = when {
        error?.hasProtonErrorCode(PASSWORD_WRONG) == true -> State.Opened.CancelPasswordReset(
            passwordError = error.message?.let {
                StringBox(it)
            } ?: StringBox(R.string.presentation_error_general),
            onCancelPasswordRequest = onCancelPasswordRequest,
            onBack = onBack
        )

        error != null -> State.Error(error)
        success == true -> State.Closed(hasCancelledSuccessfully = true)
        else -> State.Opened.CancelPasswordReset(
            processing = processing,
            passwordError = passwordError,
            onCancelPasswordRequest = onCancelPasswordRequest,
            onBack = onBack
        )
    }
}

internal fun State.toScreenId(): ScreenId? =
    when (this) {
        is State.Closed -> null
        is State.Error -> null
        is State.Loading -> null
        is State.StartPasswordManager -> null
        is State.Opened.CancellationHappened -> ScreenId.recoveryCancelledInfo
        is State.Opened.GracePeriodStarted -> ScreenId.gracePeriodInfo
        is State.Opened.CancelPasswordReset -> ScreenId.cancelResetPassword
        is State.Opened.PasswordChangePeriodStarted -> ScreenId.passwordChangeInfo
        is State.Opened.RecoveryEnded -> ScreenId.recoveryExpiredInfo
    }

private fun Duration.toHoursCeil(): Long {
    val millisPerHour = 1.hours.inWholeMilliseconds.toDouble()
    val hours = inWholeMilliseconds / millisPerHour
    return ceil(hours).toLong()
}
