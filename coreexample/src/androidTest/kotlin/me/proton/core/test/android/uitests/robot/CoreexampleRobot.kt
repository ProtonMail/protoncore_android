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

import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import androidx.annotation.IdRes
import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.auth.AccountSwitcherRobot
import me.proton.core.test.android.robots.auth.signup.ChooseExternalEmailRobot
import me.proton.core.test.android.robots.auth.signup.ChooseInternalEmailRobot
import me.proton.core.test.android.robots.auth.signup.ChooseUsernameRobot
import me.proton.core.test.android.robots.confirmpassword.ConfirmPasswordRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.robots.settings.RecoveryEmailRobot
import me.proton.core.test.android.robots.settings.PasswordManagementRobot

/**
 * [CoreexampleRobot] class contains actions and verifications for Main screen functionality.
 */
open class CoreexampleRobot : CoreRobot() {
    fun bugReport(waitForServer: Boolean = false): BugReportRobot {
        val buttonId = if (waitForServer) R.id.bugReportWaiting else R.id.bugReport
        return scrollToAndClick(buttonId)
    }

    fun humanVerification(): HVRobot = scrollToAndClick(R.id.trigger_human_ver)
    fun signupInternal(): ChooseInternalEmailRobot = scrollToAndClick(R.id.signupInternal)
    fun signupExternal(): ChooseExternalEmailRobot = scrollToAndClick(R.id.signupExternal)
    fun signupUsername(): ChooseUsernameRobot = scrollToAndClick(R.id.signupUsername)
    inline fun <reified T> logoutUser(user: User): T = clickUserButton(user)
    inline fun <reified T> clickUserButton(
        user: User,
        accountState: AccountState = Ready,
        sessionState: SessionState = Authenticated
    ): T = clickElement(getUserState(user, accountState, sessionState))

    fun plansUpgrade(): SelectPlanRobot = scrollToAndClick(R.id.plansUpgrade)
    fun plansCurrent(): SelectPlanRobot = scrollToAndClick(R.id.plansCurrent)
    fun settingsRecoveryEmail(): RecoveryEmailRobot = scrollToAndClick(R.id.settingsRecovery)
    fun settingsPasswordManagement(): PasswordManagementRobot = scrollToAndClick(R.id.settingsPassword)
    fun confirmPasswordLocked(): ConfirmPasswordRobot = scrollToAndClick(R.id.trigger_confirm_password_locked)
    fun confirmPasswordPassword(): ConfirmPasswordRobot = scrollToAndClick(R.id.trigger_confirm_password_pass)
    fun lockScopes(): CoreexampleRobot = scrollToAndClick(R.id.lock_scope)
    fun accountSwitcher(): AccountSwitcherRobot = scrollToAndClick(R.id.accountPrimaryView, ViewGroup::class.java)

    private inline fun <reified T> scrollToAndClick(@IdRes id: Int, clazz: Class<*> = Button::class.java): T {
        view.withId(id).scrollTo()
        return clickElement(id, clazz)
    }

    class Verify : CoreVerify() {

        fun userStateIs(user: User, accountState: AccountState, sessionState: SessionState?) {
            val userState = getUserState(user, accountState, sessionState)
            view.withId(R.id.parentSwipeView).swipeUp()
            view.withText(userState).checkDisplayed()
        }

        fun accountSwitcherDisplayed() {
            view.instanceOf(ScrollView::class.java).swipeDown()
            view.withId(R.id.account_email_textview).checkDisplayed()
        }

        fun primaryUserIs(user: User) {
            view.instanceOf(ScrollView::class.java).swipeDown()
            view.withId(R.id.account_email_textview).startsWith("${user.name}@").checkDisplayed()
        }

        fun secureScopeStateIs(vararg states: String) {
            view.withId(R.id.scope_status).scrollTo().let { view ->
                if (states.isEmpty()) {
                    view.checkContains("[]")
                } else {
                    states.forEach { view.checkContains(it) }
                }
            }
        }

        fun scopeTriggerStatusIs(vararg oneOf: String) {
            view.withId(R.id.trigger_status).scrollTo().checkContainsAny(*oneOf)
        }

        fun plansUpgradeDisabled() {
            view.withId(R.id.plansUpgrade).isDisabled()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)

    companion object {
        fun getUserState(user: User, accountState: AccountState, sessionState: SessionState?): String =
            "${user.name} -> $accountState/$sessionState".uppercase()
    }
}
