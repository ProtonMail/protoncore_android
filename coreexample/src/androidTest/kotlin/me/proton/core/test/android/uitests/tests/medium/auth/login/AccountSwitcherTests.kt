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

package me.proton.core.test.android.uitests.tests.medium.auth.login

import me.proton.core.test.android.robots.auth.AccountSwitcherRobot
import me.proton.core.test.android.robots.auth.AccountSwitcherRobot.UserAction
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.robots.auth.login.MailboxPasswordRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class AccountSwitcherTests : BaseTest() {

    private val accountSwitcherRobot = AccountSwitcherRobot()
    private val onePassUser = users.getUser { it.isPaid }

    @Before
    fun loginOnePass() {
        AddAccountRobot()
            .signIn()
            .loginUser<CoreexampleRobot>(onePassUser)
            .accountSwitcher()
            .verify { hasUser(onePassUser) }
    }

    @Test
    fun removeUser() {
        accountSwitcherRobot
            .userAction<AddAccountRobot>(onePassUser, UserAction.Remove)
            .verify { addAccountElementsDisplayed() }
    }

    @Test
    fun loginDisabledUser() {
        accountSwitcherRobot
            .userAction<AccountSwitcherRobot>(onePassUser, UserAction.SignOut)
            .userAction<LoginRobot>(onePassUser, UserAction.SignIn)
            .verify { loginElementsDisplayed() }
    }

    @Test
    fun addAndRemoveSecondAccount() {
        val twoPassUser = users.getUser(false) { it.passphrase.isNotEmpty() }
        accountSwitcherRobot
            .addAccount()
            .loginUser<MailboxPasswordRobot>(twoPassUser)
            .mailboxPassword(twoPassUser.passphrase)
            .unlock<AccountSwitcherRobot>()
            .verify {
                userEnabled(twoPassUser)
                userEnabled(onePassUser)
            }

        accountSwitcherRobot
            .back<CoreexampleRobot>()
            .verify { primaryUserIs(twoPassUser) }

        CoreexampleRobot()
            .accountSwitcher()
            .userAction<AccountSwitcherRobot>(twoPassUser, UserAction.SignOut)
            .back<CoreexampleRobot>()
            .verify { primaryUserIs(onePassUser) }
    }
}
