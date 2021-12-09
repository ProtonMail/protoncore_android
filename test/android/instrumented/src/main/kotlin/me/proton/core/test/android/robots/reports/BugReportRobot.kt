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

package me.proton.core.test.android.robots.reports

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.report.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils.pluralStringFromResource
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class BugReportRobot : CoreRobot() {
    fun description(value: String): BugReportRobot =
        apply { view.withId(R.id.bug_report_description).replaceText(value) }

    inline fun <reified T> exitDialogDiscardBugReport(): T =
        clickElement(view.withText(R.string.report_bug_discard_changes_confirm))

    inline fun <reified T> exitDialogKeepWriting(): T =
        clickElement(view.withText(R.string.report_bug_discard_changes_cancel))

    fun subject(value: String): BugReportRobot = replaceText(R.id.bug_report_subject, value)
    inline fun <reified T> send(): T = clickElement(view.withId(R.id.bug_report_send))

    class Verify : CoreVerify() {
        fun descriptionFieldHasError(@StringRes stringId: Int, vararg formatArgs: Any) = view
            .withId(R.id.bug_report_description_layout)
            .withCustomMatcher(ViewMatchers.hasErrorText(stringFromResource(stringId, *formatArgs)))

        fun descriptionFieldHasError(@PluralsRes pluralsId: Int, quantity: Int, vararg formatArgs: Any) = view
            .withId(R.id.bug_report_description_layout)
            .withCustomMatcher(ViewMatchers.hasErrorText(pluralStringFromResource(pluralsId, quantity, *formatArgs)))

        fun descriptionFieldIsDisabled() = view.withId(R.id.bug_report_description).checkDisabled()
        fun descriptionFieldIsEnabled() = view.withId(R.id.bug_report_description).checkEnabled()
        fun descriptionLength(l: Int) = view.withId(R.id.bug_report_description).checkLengthEquals(l)
        fun loaderIsDisplayed() = view.withId(R.id.bug_report_loader).checkDisabled()
        fun loaderIsHidden() = view.withId(R.id.bug_report_loader).checkDoesNotExist()
        fun sendButtonIsAvailable() = view.withId(R.id.bug_report_send).checkDisplayed().checkEnabled()
        fun subjectFieldHasError(@StringRes stringId: Int) = view
            .withId(R.id.bug_report_subject_layout)
            .withCustomMatcher(ViewMatchers.hasErrorText(stringFromResource(stringId)))

        fun sendButtonIsHidden() = view.withId(R.id.bug_report_send).checkDoesNotExist()
        fun subjectFieldIsDisabled() = view.withId(R.id.bug_report_subject).checkDisabled()
        fun subjectFieldIsEnabled() = view.withId(R.id.bug_report_subject).checkEnabled()
        fun subjectLength(l: Int) = view.withId(R.id.bug_report_subject).checkLengthEquals(l)
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
