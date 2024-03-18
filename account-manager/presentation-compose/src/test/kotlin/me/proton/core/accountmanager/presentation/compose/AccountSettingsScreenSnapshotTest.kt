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

package me.proton.core.accountmanager.presentation.compose

import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test

class AccountSettingsScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun accountSettingsScreen() {
        paparazzi.snapshot {
            ProtonTheme {
                AccountSettingsScreen(
                    onPasswordManagementClick = {},
                    onRecoveryEmailClick = {},
                    onBackClick = {}
                )
            }
        }
    }

    @Test
    fun accountSettingsScreenDark() {
        paparazzi.snapshot {
            ProtonTheme(isDark = true) {
                AccountSettingsScreen(
                    onPasswordManagementClick = {},
                    onRecoveryEmailClick = {},
                    onBackClick = {}
                )
            }
        }
    }
}
