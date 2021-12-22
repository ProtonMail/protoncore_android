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

    /** Registers a [caller] for launching report activities.
     * If the [caller] is also a lifecycle owner, this method should be called before the [caller] is STARTED.
     * @param caller A caller that will be able to launch report activities.
     * @param bugReportCallback A callback to deliver a result, after the Bug Report activity has been closed.
     */
    public fun register(caller: ActivityResultCaller, bugReportCallback: ((BugReportOutput) -> Unit)? = null) {
        bugReportLauncher?.unregister()
        bugReportLauncher = caller.registerForActivityResult(BugReportActivity.ResultContract()) {
            bugReportCallback?.invoke(it)
        }
    }

    public fun unregister() {
        bugReportLauncher?.unregister()
        bugReportLauncher = null
    }

    /** Starts a Bug Report screen with given [input].
     * Make sure that [register] method has been called, before calling this method.
     */
    public fun startBugReport(input: BugReportInput) {
        checkNotNull(bugReportLauncher) {
            "You must call reportOrchestrator.register(context, callback) before starting workflow!"
        }.launch(input)
    }
}
