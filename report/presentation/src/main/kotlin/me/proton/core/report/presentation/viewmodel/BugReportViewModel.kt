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

package me.proton.core.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportExtra
import me.proton.core.report.domain.entity.BugReportField
import me.proton.core.report.domain.entity.validate
import me.proton.core.report.domain.provider.BugReportLogProvider
import me.proton.core.report.domain.usecase.SendBugReport
import me.proton.core.report.presentation.entity.BugReportFormState
import me.proton.core.report.presentation.entity.ExitSignal
import me.proton.core.report.presentation.entity.ReportFormData
import java.util.Optional
import javax.inject.Inject

@HiltViewModel
internal class BugReportViewModel @Inject constructor(
    private val sendBugReport: SendBugReport,
    bugReportLogProvider: Optional<BugReportLogProvider>,
) : ViewModel() {
    private val _bugReportFormState = MutableStateFlow<BugReportFormState>(BugReportFormState.Idle)
    val bugReportFormState: StateFlow<BugReportFormState> = _bugReportFormState.asStateFlow()

    private val _exitSignal = MutableSharedFlow<ExitSignal>()
    val exitSignal: Flow<ExitSignal> = _exitSignal.asSharedFlow()

    private val _hideKeyboardSignal = MutableSharedFlow<Unit>()
    val hideKeyboardSignal: Flow<Unit> = _hideKeyboardSignal.asSharedFlow()

    val shouldShowAttachLog = bugReportLogProvider.isPresent

    /** Re-validates the description, and preserves other errors (if any). */
    suspend fun revalidateDescription(description: String) {
        val errors = (_bugReportFormState.value as? BugReportFormState.FormError)?.errors.orEmpty()
        val descriptionErrors = BugReport.validateDescription(description)
        val otherErrors = errors.filter { it.field != BugReportField.Description }
        _bugReportFormState.emit(BugReportFormState.FormError(descriptionErrors + otherErrors))
    }

    /** Re-validates the subject, and preserves other errors (if any). */
    suspend fun revalidateSubject(subject: String) {
        val errors = (_bugReportFormState.value as? BugReportFormState.FormError)?.errors.orEmpty()
        val titleErrors = BugReport.validateTitle(subject)
        val otherErrors = errors.filter { it.field != BugReportField.Subject }
        _bugReportFormState.emit(BugReportFormState.FormError(titleErrors + otherErrors))
    }

    suspend fun clearFormErrors(forField: BugReportField) {
        val errors = (_bugReportFormState.value as? BugReportFormState.FormError)?.errors.orEmpty()
        val otherErrors = errors.filter { it.field != forField }
        _bugReportFormState.emit(BugReportFormState.FormError(otherErrors))
    }

    fun tryExit(data: ReportFormData? = null, force: Boolean = false) {
        val formState = _bugReportFormState.value
        val signal = if (formState is BugReportFormState.SendingResult && formState.result.isPending()) {
            ExitSignal.BugReportEnqueued
        } else if (force || data?.subject.isNullOrBlank() && data?.description.isNullOrBlank()) {
            ExitSignal.ExitNow
        } else {
            ExitSignal.Ask
        }
        viewModelScope.launch { _exitSignal.emit(signal) }
    }

    fun trySendingBugReport(
        data: ReportFormData,
        email: String,
        username: String,
        country: String?,
        isp: String?,
    ) = viewModelScope.launch {
        _hideKeyboardSignal.emit(Unit)
        _bugReportFormState.emit(BugReportFormState.Processing)

        val bugReport = makeBugReport(data, email = email, username = username)
        val formErrors = bugReport.validate()

        if (formErrors.isEmpty()) {
            val extra = if (country != null || isp != null) {
                BugReportExtra(country = country, isp = isp)
            } else null
            sendBugReport(bugReport, extra).collect {
                _bugReportFormState.emit(BugReportFormState.SendingResult(it))
            }
        } else {
            _bugReportFormState.emit(BugReportFormState.FormError(formErrors))
        }
    }

    private fun makeBugReport(data: ReportFormData, email: String, username: String) = BugReport(
        title = data.subject,
        description = data.description,
        email = email,
        username = username,
        shouldAttachLog = data.attachLog,
    )
}
