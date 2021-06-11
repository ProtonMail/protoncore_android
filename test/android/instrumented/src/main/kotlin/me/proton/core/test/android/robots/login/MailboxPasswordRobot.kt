/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.test.android.robots.login

import android.widget.EditText
import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [MailboxPasswordRobot] class contains mailbox password actions and verifications implementation
 */
class MailboxPasswordRobot : CoreRobot() {

    /**
     * Fills in mailbox password input with [password] and clicks unlock button
     * @return [MailboxPasswordRobot]
     */
    fun mailboxPassword(password: String): MailboxPasswordRobot = setText(R.id.mailboxPasswordInput, password)

    /**
     * Clicks unlock button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> unlock(): T = clickElement(R.id.unlockButton)

    class Verify : CoreVerify() {
        fun mailboxPasswordElementsDisplayed() {
            view.withId(R.id.unlockButton).wait().checkDisplayed()
            view.withId(R.id.mailboxPasswordInput).instanceOf(EditText::class.java).checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
