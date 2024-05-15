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

package me.proton.core.accountmanager.test.robot

import me.proton.core.accountmanager.presentation.compose.SignOutDialogTestTag
import me.proton.test.fusion.Fusion.node

public object SignOutDialogRobot {
    private val cancelSignOutButton = node.withTag(SignOutDialogTestTag.CANCEL_SIGN_OUT)
    private val confirmSignOutButton = node.withTag(SignOutDialogTestTag.CONFIRM_SIGN_OUT)
    private val removeDataCheckbox = node.withTag(SignOutDialogTestTag.REMOVE_DATA)

    public fun cancel() {
        cancelSignOutButton.await { assertIsDisplayed() }
        cancelSignOutButton.click()
    }

    public fun confirmSignOut() {
        confirmSignOutButton.await { assertIsDisplayed() }
        confirmSignOutButton.click()
    }

    public fun toggleRemoveData() {
        removeDataCheckbox.await { assertIsDisplayed() }
        removeDataCheckbox.click()
    }
}
