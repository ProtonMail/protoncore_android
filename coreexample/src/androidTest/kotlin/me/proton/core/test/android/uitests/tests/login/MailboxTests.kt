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

package me.proton.core.test.android.uitests.tests.login

import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.instrumented.data.User.Users.getUser
import me.proton.core.test.android.instrumented.robots.login.LoginRobot
import me.proton.core.test.android.instrumented.robots.login.MailboxPasswordRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class MailboxTests : BaseTest() {

    private val loginRobot = LoginRobot()
    private val mailboxPasswordRobot = MailboxPasswordRobot()
    private val twoPassUser = getUser { it.passphrase.isNotEmpty() }

    @Before
    fun goToMailbox() {
        jailUnban()
        loginRobot
            .username(twoPassUser.name)
            .password(twoPassUser.password)
            .signIn<MailboxPasswordRobot>()
            .verify { mailboxPasswordElementsDisplayed() }
    }

    @Test
    fun incorrectMailboxPassword() {
        mailboxPasswordRobot
            .unlockMailbox<MailboxPasswordRobot>("Incorrect")
            .verify { errorSnackbarDisplayed(R.string.auth_mailbox_login_error_invalid_passphrase) }

        mailboxPasswordRobot
            .close<CoreexampleRobot>()
            .verify { userIsLoggedOut(twoPassUser) }
    }

    @Test
    fun closeMailbox() {
        mailboxPasswordRobot
            .close<CoreexampleRobot>()
            .verify { userIsLoggedOut(twoPassUser) }
    }

    @Test
    fun loginWithTwoPass() {
        mailboxPasswordRobot
            .unlockMailbox<CoreexampleRobot>(twoPassUser.passphrase)
            .verify { userStateIs(twoPassUser, Ready, Authenticated) }
    }
}
