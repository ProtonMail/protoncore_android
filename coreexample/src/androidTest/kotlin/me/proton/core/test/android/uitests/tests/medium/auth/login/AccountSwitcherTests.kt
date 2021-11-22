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
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.Before
import org.junit.Test

class AccountSwitcherTests : BaseTest() {

    private val accountSwitcherRobot = AccountSwitcherRobot()
    private val paidUsers = users.getUsers { it.isPaid }
    private val firstUser = paidUsers[0]
    private val secondUser = paidUsers[1]

    @Before
    fun loginOnePass() {
        quark.jailUnban()
        login(firstUser)

        CoreexampleRobot()
            .accountSwitcher()
            .verify { hasUser(firstUser) }
    }

    @Test
    fun removeUser() {
        accountSwitcherRobot
            .userAction<AddAccountRobot>(firstUser, UserAction.Remove)
            .verify { addAccountElementsDisplayed() }
    }

    @Test
    fun signOutAndLoginDisabledUser() {
        accountSwitcherRobot
            .userAction<AccountSwitcherRobot>(firstUser, UserAction.SignOut)
            .userAction<LoginRobot>(firstUser, UserAction.SignIn)
            .verify { loginElementsDisplayed() }
    }

    @Test
    @SmokeTest
    fun addAndRemoveSecondAccount() {
        accountSwitcherRobot
            .addAccount()
            .loginUser<AccountSwitcherRobot>(secondUser)
            .verify {
                userEnabled(secondUser)
                userEnabled(firstUser)
            }

        accountSwitcherRobot
            .back<CoreexampleRobot>()
            .verify { primaryUserIs(secondUser) }

        CoreexampleRobot()
            .accountSwitcher()
            .userAction<AccountSwitcherRobot>(secondUser, UserAction.SignOut)
            .back<CoreexampleRobot>()
            .verify { primaryUserIs(firstUser) }
    }
}
