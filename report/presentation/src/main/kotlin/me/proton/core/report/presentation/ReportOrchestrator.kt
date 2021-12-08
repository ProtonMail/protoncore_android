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

package me.proton.core.report.presentation

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.report.presentation.entity.BugReportInput
import me.proton.core.report.presentation.entity.BugReportOutput
import me.proton.core.report.presentation.ui.BugReportActivity
import javax.inject.Inject

public class ReportOrchestrator @Inject constructor() {
    private var bugReportLauncher: ActivityResultLauncher<BugReportInput>? = null
    private var bugReportResultCallback: ((BugReportOutput) -> Unit)? = null

    public fun register(caller: ActivityResultCaller) {
        bugReportLauncher = caller.registerForActivityResult(BugReportActivity.ResultContract()) {
            bugReportResultCallback?.invoke(it)
        }
    }

    public fun unregister() {
        bugReportLauncher?.unregister()
        bugReportLauncher = null
        bugReportResultCallback = null
    }

    public fun startBugReport(input: BugReportInput) {
        checkRegistered(bugReportLauncher).launch(input)
    }

    public fun setOnBugReportResult(block: (BugReportOutput) -> Unit) {
        bugReportResultCallback = block
    }

    private fun <T> checkRegistered(launcher: T?) =
        checkNotNull(launcher) { "You must call reportOrchestrator.register(context) before starting workflow!" }
}
