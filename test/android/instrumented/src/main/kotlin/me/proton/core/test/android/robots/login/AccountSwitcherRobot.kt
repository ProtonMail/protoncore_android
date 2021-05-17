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

package me.proton.core.test.android.robots.login

import me.proton.core.accountmanager.presentation.R
import me.proton.core.test.android.instrumented.builders.OnView
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [AccountSwitcherRobot] class contains account switcher actions and verifications implementation
 */
class AccountSwitcherRobot : CoreRobot() {

    /**
     * User can be in three states - authenticated, disabled, removed
     * Logout - disables user, but keeps information
     * Login - logs in disabled user
     * Remove - completely removes any user
     */
    enum class UserAction {
        Login, Remove, Logout
    }

    /**
     * [UserElement] data class describes a user view group inside the account switcher
     */
    data class UserElement(
        val user: User
    ) {
        private val emailView = OnView().withText(user.name + "@proton.black")
        val moreButton = OnView().withId(R.id.account_more_button).hasSibling(emailView)
        val userViewGroup = OnView().withChild(moreButton)
    }

    /**
     * Clicks a view group of a given user
     * @return [AccountSwitcherRobot]
     */
    fun selectUser(user: User): AccountSwitcherRobot {
        UserElement(user).userViewGroup.click()
        return this
    }

    /**
     * Clicks 'more button' in user view group. Clicks a given [UserAction]
     * @return [AccountSwitcherRobot] or [LoginRobot]
     */
    fun userAction(user: User, action: UserAction): CoreRobot {
        UserElement(user).moreButton.click()
        view.withText(action.toString()).click()
        return if (action == UserAction.Login) LoginRobot() else this
    }

    /**
     * Clicks 'add account' button
     * @return [LoginRobot]
     */
    fun addAccount(): LoginRobot = clickElement(R.id.account_action_textview)

    class Verify : CoreVerify() {

        fun hasUser(user: User) {
            val userElement = UserElement(user)
            userElement.moreButton.wait()
            userElement.userViewGroup.wait()
        }

        fun userDisabled(user: User) {
            UserElement(user).userViewGroup.checkDisabled()
        }

        fun userEnabled(user: User) {
            UserElement(user).userViewGroup.checkEnabled()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
