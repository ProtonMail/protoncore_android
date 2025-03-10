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

package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import org.junit.Rule
import org.junit.Test

class MemberApprovalScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun signInRequestedForApprovalScreenTest() {
        paparazzi.snapshot {
            MemberApprovalScreen(
                onCloseClicked = {},
                onConfirmClicked = {},
                onRejectClicked = {},
                state = MemberApprovalState.Idle(
                    data = MemberApprovalData(
                        email = "user@example.test",
                        pendingDevices = listOf(
                            AuthDeviceData(
                                deviceId = AuthDeviceId("id"),
                                name = "Google Pixel 8",
                                localizedClientName = "Proton for Android",
                                platform = AuthDevicePlatform.Android,
                                lastActivityTime = 0,
                                lastActivityReadable = "10:22 AM"
                            )
                        )
                    ),
                )
            )
        }
    }
}
