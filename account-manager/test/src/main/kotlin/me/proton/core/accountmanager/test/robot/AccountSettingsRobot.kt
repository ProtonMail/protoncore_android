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

package me.proton.core.accountmanager.test.robot

import me.proton.core.accountmanager.presentation.R
import me.proton.test.fusion.Fusion

public object AccountSettingsRobot {

    private val passwordManagement =
        Fusion.node.withText(R.string.account_settings_list_item_password_header)
    private val recoveryEmail =
        Fusion.node.withText(R.string.account_settings_list_item_recovery_header)

    public fun clickPasswordManagement() {
        passwordManagement.click()
    }

    public fun clickRecoveryEmail() {
        recoveryEmail.click()
    }
}
