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

package me.proton.core.report.presentation.entity

import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.usecase.SendBugReport

internal sealed class BugReportFormState {
    object Idle : BugReportFormState()
    object Processing : BugReportFormState()
    data class FormError(val errors: List<BugReportValidationError>) : BugReportFormState()
    data class SendingResult(val result: SendBugReport.Result) : BugReportFormState()
}

internal data class ReportFormData(
    val subject: String,
    val description: String,
    val attachLog: Boolean = false,
)

internal enum class ExitSignal {
    /** User will need to confirm the exit. */
    Ask,

    /** Bug report was enqueued and will be sent, even if the user exists the Bug Report screen. */
    BugReportEnqueued,

    /** Closes the Bug Report screen immediately. */
    ExitNow
}
