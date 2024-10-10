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

package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import kotlin.test.Test

class BackupPasswordChangeScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `idle screen`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordChangeScreen(
                    state = BackupPasswordChangeState.Idle
                )
            }
        }
    }

    @Test
    fun `password too short`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordChangeScreen(
                    state = BackupPasswordChangeState.FormError(PasswordFormError.PasswordTooShort)
                )
            }
        }
    }

    @Test
    fun `passwords do not match`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordChangeScreen(
                    state = BackupPasswordChangeState.FormError(PasswordFormError.PasswordsDoNotMatch)
                )
            }
        }
    }
}
