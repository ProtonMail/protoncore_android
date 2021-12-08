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

package me.proton.core.reports.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportExtra
import me.proton.core.report.domain.entity.validate
import me.proton.core.report.domain.usecase.SendBugReport
import me.proton.core.reports.presentation.entity.BugReportFormState
import me.proton.core.reports.presentation.entity.ExitSignal
import me.proton.core.reports.presentation.entity.ReportFormData
import javax.inject.Inject

@HiltViewModel
internal class BugReportViewModel @Inject constructor(
    private val sendBugReport: SendBugReport
) : ViewModel() {
    private val _bugReportFormState = MutableStateFlow<BugReportFormState>(BugReportFormState.Idle)
    val bugReportFormState: StateFlow<BugReportFormState> = _bugReportFormState.asStateFlow()

    private val _exitSignal = MutableSharedFlow<ExitSignal>()
    val exitSignal: Flow<ExitSignal> = _exitSignal.asSharedFlow()

    fun tryExit(data: ReportFormData? = null, force: Boolean = false) {
        val formState = _bugReportFormState.value
        val signal = if (formState is BugReportFormState.SendingResult && formState.result.isBlockedOrEnqueued()) {
            ExitSignal.BugReportEnqueued
        } else if (force || data?.subject.isNullOrBlank() && data?.description.isNullOrBlank()) {
            ExitSignal.ExitNow
        } else {
            ExitSignal.Ask
        }
        viewModelScope.launch { _exitSignal.emit(signal) }
    }

    fun trySendingBugReport(data: ReportFormData, email: String, username: String) = viewModelScope.launch {
        _bugReportFormState.emit(BugReportFormState.Processing)

        val bugReport = makeBugReport(data, email = email, username = username)
        bugReport.validate()?.let {
            _bugReportFormState.emit(BugReportFormState.FormError(it))
            return@launch
        }

        val extra = if (data.country != null || data.isp != null) {
            BugReportExtra(country = data.country, isp = data.isp)
        } else null

        sendBugReport(bugReport, extra).collect {
            _bugReportFormState.emit(BugReportFormState.SendingResult(it))
        }
    }

    private fun makeBugReport(data: ReportFormData, email: String, username: String) = BugReport(
        title = data.subject,
        description = data.description,
        email = email,
        username = username
    )
}
