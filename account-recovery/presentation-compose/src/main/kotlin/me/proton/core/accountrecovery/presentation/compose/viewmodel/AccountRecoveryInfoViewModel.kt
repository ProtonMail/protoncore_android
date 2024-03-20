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

package me.proton.core.accountrecovery.presentation.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DateTimeFormat.DateTimeForm
import me.proton.core.util.android.datetime.DurationFormat
import me.proton.core.util.android.datetime.UtcClock
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@HiltViewModel
class AccountRecoveryInfoViewModel @Inject constructor(
    accountManager: AccountManager,
    observeUserRecovery: ObserveUserRecovery,
    @UtcClock private val clock: Clock,
    private val dateTimeFormat: DateTimeFormat,
    private val durationFormat: DurationFormat,
) : ViewModel() {

    private val primaryUserId = accountManager.getPrimaryUserId().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = null
    )

    val state: StateFlow<AccountRecoveryInfoViewState> = primaryUserId
        .filterNotNull()
        .flatMapLatest { observeUserRecovery(it) }
        .map { recovery ->
            when (recovery?.state?.enum) {
                null -> AccountRecoveryInfoViewState.None
                UserRecovery.State.None -> AccountRecoveryInfoViewState.None
                else -> getRecoveryState(recovery)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = AccountRecoveryInfoViewState.None
        )

    private fun getRecoveryState(recovery: UserRecovery): AccountRecoveryInfoViewState.Recovery {
        val startDateFormatted = dateTimeFormat.format(recovery.startTime, DateTimeForm.MEDIUM_DATE)
        val endDateFormatted = dateTimeFormat.format(recovery.endTime, DateTimeForm.MEDIUM_DATE)

        val end = recovery.endTime
        val duration = (end - clock.currentEpochSeconds()).seconds
        val durationFormatted =
            durationFormat.format(duration, DurationUnit.HOURS, DurationUnit.MINUTES)

        return AccountRecoveryInfoViewState.Recovery(
            recoveryState = recovery.state.enum,
            startDate = startDateFormatted,
            endDate = endDateFormatted,
            durationUntilEnd = durationFormatted
        )
    }
}

sealed class AccountRecoveryInfoViewState {
    data object None : AccountRecoveryInfoViewState()
    data class Recovery(
        val recoveryState: UserRecovery.State?,
        val startDate: String,
        val endDate: String,
        val durationUntilEnd: String
    ) : AccountRecoveryInfoViewState()
}
