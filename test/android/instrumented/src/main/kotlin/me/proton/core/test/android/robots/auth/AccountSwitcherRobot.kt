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

package me.proton.core.test.android.robots.auth

import android.widget.TextView
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import me.proton.core.accountmanager.presentation.R
import me.proton.core.test.android.instrumented.builders.OnView
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.auth.login.LoginRobot
import org.hamcrest.CoreMatchers.startsWith

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
    enum class UserAction(@StringRes val resId: Int) {
        SignIn(R.string.account_switcher_action_sign_in),
        Remove(R.string.account_switcher_action_remove),
        SignOut(R.string.account_switcher_action_sign_out)
    }

    /**
     * Clicks a view group of a given user
     * @return [AccountSwitcherRobot]
     */
    fun selectUser(user: User): AccountSwitcherRobot = clickElement(userEmail(user))

    /**
     * Clicks 'more button' in user view group. Clicks a given [UserAction]
     * @return [AccountSwitcherRobot] or [LoginRobot]
     */
    inline fun <reified T> userAction(user: User, action: UserAction): T {
        userMore(user).click()
        Espresso
            .onView(ViewMatchers.withText(action.resId))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(ViewActions.click())
        return T::class.java.newInstance()
    }

    /**
     * Clicks 'add account' button
     * @return [LoginRobot]
     */
    fun addAccount(): LoginRobot = clickElement(R.id.account_action_textview, TextView::class.java)

    companion object UserElement : CoreRobot() {
        fun userEmail(user: User): OnView =
            view
                .withId(R.id.account_email_textview)
                .startsWith("${user.name}@")
                .wait()

        fun userMore(user: User): OnView =
            view
                .withId(R.id.account_more_button)
                .hasSibling(userEmail(user))
                .wait()
    }

    class Verify : CoreVerify() {
        fun hasUser(user: User) {
            userEmail(user).checkDisplayed()
            userMore(user).checkDisplayed()
        }

        fun userDisabled(user: User) {
            userEmail(user).checkDisabled()
            userMore(user).checkEnabled()
        }

        fun userEnabled(user: User) {
            userEmail(user).checkEnabled()
            userMore(user).checkEnabled()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
