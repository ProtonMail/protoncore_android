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

package me.proton.android.core.coreexample.viewmodel

import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.report.presentation.ReportOrchestrator
import me.proton.core.report.presentation.entity.BugReportInput
import me.proton.core.report.presentation.entity.BugReportOutput
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val reportOrchestrator: ReportOrchestrator,
    private val userManager: UserManager
) : ViewModel() {
    private val _bugReportSent = Channel<String>()
    val bugReportSent: Flow<String> = _bugReportSent.receiveAsFlow()

    fun register(caller: ActivityResultCaller) {
        reportOrchestrator.register(caller) {
            if (it is BugReportOutput.SuccessfullySent) {
                viewModelScope.launch { _bugReportSent.send(it.successMessage) }
            }
        }
    }

    override fun onCleared() {
        reportOrchestrator.unregister()
        _bugReportSent.close()
        super.onCleared()
    }

    fun reportBugs(waitForServer: Boolean) = viewModelScope.launch {
        val userId = accountManager.getPrimaryUserId().first()
        val user = userId?.let { userManager.getUser(it) }
        val email = user?.email ?: "test-bug-report@proton.black"
        val username = user?.name ?: "test-bug-report"
        val input = BugReportInput(email = email, username = username, finishAfterReportIsEnqueued = !waitForServer)
        reportOrchestrator.startBugReport(input)
    }
}
