/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.signin

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class SignInScreenTest(deviceConfig: DeviceConfig) {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = deviceConfig)

    @Test
    fun `loading state`() {
        paparazzi.snapshot {
            ProtonTheme {
                SignInScreen(state = SignInState.Loading)
            }
        }
    }

    @Test
    fun `idle state`() {
        paparazzi.snapshot {
            ProtonTheme {
                SignInScreen(
                    state = SignInState.Idle(
                        errorMessage = null,
                        qrCode = "qr-code",
                        generateBitmap = { _, _ -> throw NotImplementedError() }
                    ))
            }
        }
    }

    @Test
    fun `unrecoverable error`() {
        paparazzi.snapshot {
            ProtonTheme {
                SignInScreen(state = SignInState.Failure(message = "Error", onRetry = {}))
            }
        }
    }

    companion object {
        @Parameters
        @JvmStatic
        fun parameters() = listOf(
            DeviceConfig.PIXEL_5,
            DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT)
        )
    }
}
