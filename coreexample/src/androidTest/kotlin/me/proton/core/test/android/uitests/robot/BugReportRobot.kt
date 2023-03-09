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

package me.proton.core.test.android.uitests.robot

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.report.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils.pluralStringFromResource
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class BugReportRobot : CoreRobot() {

    /**
     * Replaces bug report title with given [value]
     */
    fun subject(value: String): BugReportRobot = replaceText(R.id.bug_report_subject, value)

    /**
     * Replaces bug report description with given [value]
     */
    fun description(value: String): BugReportRobot = replaceText(R.id.bug_report_description, value)

    /**
     * Clicks "Discard" button
     */
    inline fun <reified T> discard(): T = clickElement(stringFromResource(R.string.core_report_bug_discard_changes_confirm))

    /**
     * Clicks "Keep writing" button
     */
    fun keepWriting(): BugReportRobot = clickElement(stringFromResource(R.string.core_report_bug_discard_changes_cancel))

    /**
     * Clicks "Send" button
     */
    inline fun <reified T> send(): T = clickElement(view.withId(R.id.bug_report_send))

    class Verify : CoreVerify() {
        fun subjectFieldHasError(@StringRes stringId: Int) = view
            .withId(R.id.bug_report_subject_layout)
            .withCustomMatcher(ViewMatchers.hasErrorText(stringFromResource(stringId)))

        fun descriptionFieldHasError(@PluralsRes pluralsId: Int, quantity: Int, vararg formatArgs: Any) = view
            .withId(R.id.bug_report_description_layout)
            .withCustomMatcher(ViewMatchers.hasErrorText(pluralStringFromResource(pluralsId, quantity, *formatArgs)))

        fun bugReportIsSending() {
            view.withId(R.id.bug_report_description).checkDisabled()
            view.withId(R.id.bug_report_subject).checkDisabled()
            view.withId(R.id.bug_report_loader).checkDisabled()
            view.withId(R.id.bug_report_send).checkDoesNotExist()
        }

        fun bugReportFormIsEditable() {
            view.withId(R.id.bug_report_description).checkEnabled()
            view.withId(R.id.bug_report_subject).checkEnabled()
            view.withId(R.id.bug_report_send).checkDisplayed().checkEnabled()
            view.withId(R.id.bug_report_loader).checkDoesNotExist()
        }

        fun descriptionLength(l: Int) = view.withId(R.id.bug_report_description).checkLengthEquals(l)
        fun subjectLength(l: Int) = view.withId(R.id.bug_report_subject).checkLengthEquals(l)
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
