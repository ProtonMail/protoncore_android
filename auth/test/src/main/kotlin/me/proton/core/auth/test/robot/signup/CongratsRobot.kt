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

package me.proton.core.auth.test.robot.signup

import me.proton.core.auth.presentation.R
import me.proton.test.fusion.Fusion.view
import kotlin.time.Duration.Companion.milliseconds

public object CongratsRobot {

    private val congrats = view.withText(R.string.auth_signup_congratulations)
    private val startUsingProtonButton = view.withId(R.id.startUsingProton)

    public fun clickStart() {
        startUsingProtonButton.click()
    }

    public fun uiElementsDisplayed() {
        congrats.await(30000.milliseconds) { checkIsDisplayed() }
    }
}
