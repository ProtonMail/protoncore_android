/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.test.robot

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import me.proton.core.auth.presentation.R
import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.test.android.actions.ClickableSpanClickAction
import me.proton.test.fusion.Fusion.intent
import me.proton.test.fusion.Fusion.view

public object CredentialLessWelcomeRobot {
    private val appContext: Context
        get() = ApplicationProvider.getApplicationContext()
    private val continueAsGuestButton = view.withId(R.id.sign_in_guest)
    private val noLogsButton = view.withId(R.id.no_logs_button)
    private val signInButton = view.withId(R.id.sign_in)

    @StringRes
    private val noLogsLinkResId = R.string.vpn_no_logs_link

    public fun clickNoLogsLinkButton() {
        noLogsButton.click()
    }

    public fun clickTermsAndConditionsLink() {
        view.withId(R.id.terms).perform(ClickableSpanClickAction())
    }

    public fun clickSignIn(): LoginRobot {
        signInButton.click()
        return LoginRobot
    }

    public fun clickContinueAsGuest() {
        continueAsGuestButton.click()
    }

    public fun noLogsLinkBrowserOpened() {
        intent.checkBrowserOpened(appContext.getString(noLogsLinkResId))
    }

    public fun termsAndConditionsOpened() {
        view.withText(R.string.auth_signup_terms_conditions).await { checkIsDisplayed() }
    }
}
