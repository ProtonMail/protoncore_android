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

package me.proton.core.test.android.uitests.tests.medium.reports

import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.presentation.R
import me.proton.core.test.android.instrumented.ui.espresso.OnView
import me.proton.core.test.android.robots.reports.BugReportRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Test

class BugReportsTests : BaseTest() {
    private val closeButton: OnView
        get() = BugReportRobot().view.withContentDesc(R.string.bug_report_close)

    @Test
    fun showErrorsIfEmptyForm() {
        startBugReport()
            .apply { verify { formIsEditable() } }
            .send<BugReportRobot>()
            .verify {
                formIsEditable()
                subjectFieldHasError(R.string.bug_report_form_field_required)
                descriptionFieldHasError(R.string.bug_report_form_field_required)
            }
    }

    @Test
    fun showErrorsIfBlankForm() {
        startBugReport()
            .subject("   ")
            .description("   ")
            .send<BugReportRobot>()
            .verify {
                formIsEditable()
                subjectFieldHasError(R.string.bug_report_form_field_required)
                descriptionFieldHasError(R.string.bug_report_form_field_required)
            }
    }

    @Test
    fun descriptionTooShort() {
        startBugReport()
            .subject("Test subject")
            .description("Too short")
            .send<BugReportRobot>()
            .verify {
                formIsEditable()
                descriptionFieldHasError(
                    R.plurals.bug_report_form_field_too_short,
                    BugReport.DescriptionMinLength,
                    BugReport.DescriptionMinLength
                )
            }
    }

    @Test
    fun descriptionTooLong() {
        startBugReport()
            .description("d".repeat(BugReport.DescriptionMaxLength + 10))
            .verify {
                descriptionLength(BugReport.DescriptionMaxLength)
            }
    }

    @Test
    fun subjectTooLong() {
        startBugReport()
            .subject("s".repeat(BugReport.SubjectMaxLength + 10))
            .verify {
                subjectLength(BugReport.SubjectMaxLength)
            }
    }

    @Test
    fun keepWriting() {
        startBugReport()
            .subject("Test subject")
            .clickElement<BugReportRobot>(closeButton)
            .exitDialogKeepWriting<BugReportRobot>()
            .verify {
                formIsEditable()
            }
    }

    @Test
    fun discardEmptyForm() {
        startBugReport()
            .clickElement<CoreexampleRobot>(closeButton)
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun discardBlankForm() {
        startBugReport()
            .subject("   ")
            .description("   ")
            .clickElement<CoreexampleRobot>(closeButton)
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun discardNonEmptyForm() {
        startBugReport()
            .description("Description")
            .clickElement<BugReportRobot>(closeButton)
            .exitDialogDiscardBugReport<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun bugReportSuccessfullySent() {
        startBugReport()
            .subject("Test subject")
            .description("Test bug report")
            .send<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                snackbarDisplayed(R.string.bug_report_success)
            }
    }

    @Test
    fun successfulBugReportWithLogin() {
        login(users.getUser())
        CoreexampleRobot()
            .bugReport(waitForServer = true)
            .subject("Test subject")
            .description("Test bug report")
            .send<BugReportRobot>()
            .verify {
                descriptionFieldIsDisabled()
                subjectFieldIsDisabled()
                loaderIsDisplayed()
                sendButtonIsHidden()
            }

        CoreexampleRobot().verify {
            accountSwitcherDisplayed()
            snackbarDisplayed(R.string.bug_report_success)
        }
    }

    private fun BugReportRobot.Verify.formIsEditable() {
        descriptionFieldIsEnabled()
        subjectFieldIsEnabled()
        sendButtonIsAvailable()
        loaderIsHidden()
    }

    /** Starts the Bug Report form, without logging in. */
    private fun startBugReport(): BugReportRobot {
        return CoreexampleRobot()
            .back<CoreexampleRobot>()
            .bugReport()
    }
}
