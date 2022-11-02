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

package me.proton.core.test.android.uitests.tests.large.auth

import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class MultipleAccountAdditionTests(
    private val user: User
) : BaseTest(false) {
    @Test
    fun loginUser() {
        AddAccountRobot()
            .signIn()
            .loginUser<CoreexampleRobot>(user)
            .verify { userStateIs(user, Ready, Authenticated) }

        CoreexampleRobot()
            .accountSwitcher()
            .verify {
                data().filter { data().indexOf(it) <= data().indexOf(user) }.forEach {
                    userEnabled(it)
                }
            }
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun data() = users.getUsers { it.isPaid }
    }
}
