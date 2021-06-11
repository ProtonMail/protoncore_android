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

package me.proton.core.test.android.uitests

import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.instrumented.builders.OnView
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.signup.SignupRobot

/**
 * [CoreexampleRobot] class contains actions and verifications for Main screen functionality.
 */
open class CoreexampleRobot : CoreRobot() {

    fun humanVerification(): HumanVerificationRobot = clickElement(R.id.trigger_human_ver)
    inline fun <reified T> upgradePrimary(): T = clickElement(R.id.payment)
    fun signup(): SignupRobot = clickElement(R.id.signup)
    fun signupExternal(): SignupRobot = clickElement(R.id.signupExternal)
    inline fun <reified T> logoutUser(user: User): T = clickUserButton(user)
    inline fun <reified T> clickUserButton(
        user: User,
        accountState: AccountState = Ready,
        sessionState: SessionState = Authenticated
    ): T = clickElement(getUserState(user, accountState, sessionState))


    class Verify : CoreVerify() {

        fun userIsLoggedOut(user: User): OnView =
            view
                .withText(user.name)
                .checkDoesNotExist()

        fun primaryUserIs(user: User?): OnView =
            view
//                .withId(R.id.primaryAccountText)
                .withText("Primary: ${user?.name}")
                .wait()

        fun userStateIs(user: User, accountState: AccountState, sessionState: SessionState): OnView {
            val userState = getUserState(user, accountState, sessionState)
            return view.withText(userState).wait().checkDisplayed()
        }

    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)

    companion object {
        fun getUserState(user: User, accountState: AccountState, sessionState: SessionState): String =
            "${user.name} -> $accountState/$sessionState".toUpperCase()
    }
}
