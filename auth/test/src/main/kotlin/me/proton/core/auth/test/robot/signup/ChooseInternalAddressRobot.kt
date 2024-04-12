/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.test.robot.signup

import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view
import org.hamcrest.CoreMatchers
import kotlin.time.Duration.Companion.seconds

/** Corresponds to [me.proton.core.auth.presentation.ui.ChooseAddressActivity]. */
public object ChooseInternalAddressRobot {
    private val domainInput = view.withId(R.id.domainInput)
    private val usernameInput = view.withCustomMatcher(inputFieldMatcher(R.id.usernameInput))
    private val continueButton = view.withId(R.id.nextButton)
    private val cancelButton = view.withId(R.id.cancelButton)

    public fun fillUsername(username: String): ChooseInternalAddressRobot = apply {
        usernameInput.typeText(username)
    }

    public fun cancel() {
        cancelButton.click()
    }

    public fun next() {
        continueButton.click()
    }

    public fun selectAlternativeDomain(): ChooseInternalAddressRobot = apply {
        domainInput.click()

        view
            .containsText("protonmail")
            .withRootMatcher(isPlatformPopup())
            .click()
    }

    public fun selectPrimaryDomain(): ChooseInternalAddressRobot = apply {
        domainInput.click()

        view
            .withCustomMatcher(ViewMatchers.withText(CoreMatchers.not(CoreMatchers.containsString("protonmail"))))
            .withRootMatcher(isPlatformPopup())
            .click()
    }

    public fun screenIsDisplayed() {
        view.withText(R.string.auth_create_address_title)
            .await(60.seconds) {
                checkIsDisplayed()
            }
    }

    public fun continueButtonIsEnabled() {
        continueButton.await {
            checkIsDisplayed()
            checkIsEnabled()
        }
    }

    public fun domainInputDisplayed() {
        domainInput.await {
            checkIsDisplayed()
        }
    }

    public fun usernameInputIsEmpty() {
        usernameInput.await {
            checkLengthEquals(0)
        }
    }

    public fun usernameInputIsFilled(with: String) {
        usernameInput.await {
            checkContainsText(with)
        }
    }
}
