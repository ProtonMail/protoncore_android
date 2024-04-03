/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.report.test.robot

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import me.proton.core.report.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.report.presentation.ui.BugReportActivity]. */
public object ReportRobot {
    private val subjectInput = view.withCustomMatcher(inputFieldMatcher(R.id.bug_report_subject))
    private val descriptionInput = view.withCustomMatcher(inputFieldMatcher(R.id.bug_report_description))
    private val attachLogCheckBox = view.withId(R.id.bug_report_attach_log)
    private val sendButton = view.withId(R.id.bug_report_send)

    public fun fillSubject(subject: String): ReportRobot = apply {
        subjectInput.typeText(subject)
    }

    public fun fillDescription(description: String): ReportRobot = apply {
        descriptionInput.typeText(description)
    }

    public fun checkAttachLog() {
        attachLogCheckBox
            .interaction
            .check(matches(isNotChecked()))
            .perform(click())
    }

    public fun uncheckAttachLog() {
        attachLogCheckBox
            .interaction
            .check(matches(isChecked()))
            .perform(click())
    }

    public fun clickAttachLog() {
        attachLogCheckBox.click()
    }

    public fun clickSend() {
        sendButton.click()
    }
}
