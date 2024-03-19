/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.test.android.withToastAwait
import me.proton.test.fusion.Fusion
import me.proton.test.fusion.Fusion.node
import kotlin.time.Duration.Companion.seconds

public object CancelResetDialogRobot {

    private val currentPassword = node.withTag("PROTON_OUTLINED_TEXT_INPUT_TAG")
    private val cancelResetButton = node.withText(R.string.account_recovery_cancel_now)
    private val backButton = node.withText(R.string.account_recovery_cancel_back)
    private val dialogTitle = node.withText(R.string.account_recovery_reset_dialog_title)

    public fun fillPassword(
        password: String
    ): CancelResetDialogRobot = apply {
        currentPassword.typeText(password)
    }

    public fun clickCancelReset(): CancelResetDialogRobot = apply {
        cancelResetButton.click()
    }

    public fun clickBack(): CancelResetDialogRobot = apply {
        backButton.click()
    }

    public fun uiElementsDisplayed(): CancelResetDialogRobot = apply {
        dialogTitle.await { assertIsDisplayed() }
        cancelResetButton.await { assertIsDisplayed() }
        backButton.await { assertIsDisplayed() }
    }

    public fun successCancelResetIsDisplayed(): CancelResetDialogRobot = apply {
        Fusion.view.withToastAwait(
            text = R.string.account_recovery_cancelled_title,
            timeout = 20.seconds
        ) {
            checkIsDisplayed()
        }
    }
}
