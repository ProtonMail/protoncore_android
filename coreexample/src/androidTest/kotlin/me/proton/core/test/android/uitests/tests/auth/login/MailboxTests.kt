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

package me.proton.core.test.android.uitests.tests.auth.login

import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState.Disabled
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.plugins.Requests.jailUnban
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.MailboxPasswordRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class MailboxTests : BaseTest() {

    private val twoPassUser = users.getUser { it.passphrase.isNotEmpty() }
    private val mailboxPasswordRobot = MailboxPasswordRobot()

    @Before
    fun goToMailbox() {
        jailUnban()
        AddAccountRobot()
            .signIn()
            .username(twoPassUser.name)
            .password(twoPassUser.password)
            .signIn<MailboxPasswordRobot>()
            .verify { mailboxPasswordElementsDisplayed() }
    }

    @Test
    fun incorrectMailboxPassword() {
        mailboxPasswordRobot
            .mailboxPassword("Incorrect")
            .unlock<MailboxPasswordRobot>()
            .verify { errorSnackbarDisplayed(R.string.auth_mailbox_login_error_invalid_passphrase) }

        mailboxPasswordRobot
            .close<CoreexampleRobot>()
            .verify { userStateIs(twoPassUser, Disabled, null) }
    }

    @Test
    fun closeMailbox() {
        mailboxPasswordRobot
            .close<CoreexampleRobot>()
            .verify { coreexampleElementsDisplayed() }
    }

    @Test
    fun loginWithTwoPass() {
        mailboxPasswordRobot
            .mailboxPassword(twoPassUser.passphrase)
            .unlock<CoreexampleRobot>()
            .verify { userStateIs(twoPassUser, Ready, Authenticated) }
    }
}
