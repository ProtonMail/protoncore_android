/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.test.robot

import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.test.fusion.Fusion.node
import me.proton.test.fusion.ui.compose.ComposeWaiter.waitFor

public class AccountRecoveryGracePeriodRobot {
    private val cancelRecoveryButton = node.withText(R.string.account_recovery_cancel)
    private val continueButton = node.withText(R.string.presentation_alert_ok)
    private val dialogTitle = node.withText(R.string.account_recovery_grace_started_title)

    public fun clickContinue() {
        continueButton.click()
    }

    public fun uiElementsDisplayed() {
        node.waitFor {
            dialogTitle.assertIsDisplayed()
            cancelRecoveryButton.assertIsDisplayed()
            continueButton.assertIsDisplayed()
        }
    }
}
