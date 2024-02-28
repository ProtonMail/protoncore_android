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

package me.proton.core.accountrecovery.presentation.compose.view

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryInfoViewState
import me.proton.core.user.domain.entity.UserRecovery
import org.junit.Rule
import org.junit.Test

class AccountRecoveryInfoSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme",
    )

    @Test
    fun userRecoveryInfoGraceCollapsed() {
        paparazzi.snapshot {
            AccountRecoveryInfoGrace(
                state = AccountRecoveryInfoViewState.Recovery(
                    recoveryState = UserRecovery.State.Grace,
                    startDate = "August 11",
                    endDate = "August 14",
                    durationUntilEnd = "48h"
                ),
                expanded = false
            )
        }
    }

    @Test
    fun userRecoveryInfoGrace() {
        paparazzi.snapshot {
            AccountRecoveryInfoGrace(
                state = AccountRecoveryInfoViewState.Recovery(
                    recoveryState = UserRecovery.State.Grace,
                    startDate = "August 11",
                    endDate = "August 14",
                    durationUntilEnd = "48h"
                )
            )
        }
    }

    @Test
    fun userRecoveryInfoCancelled() {
        paparazzi.snapshot {
            AccountRecoveryInfoCancelled(
                state = AccountRecoveryInfoViewState.Recovery(
                    recoveryState = UserRecovery.State.Cancelled,
                    startDate = "August 11",
                    endDate = "August 14",
                    durationUntilEnd = "48h"
                )
            )
        }
    }

    @Test
    fun userRecoveryInfoInsecure() {
        paparazzi.snapshot {
            AccountRecoveryInfoInsecure(
                state = AccountRecoveryInfoViewState.Recovery(
                    recoveryState = UserRecovery.State.Insecure,
                    startDate = "August 11",
                    endDate = "August 14",
                    durationUntilEnd = "48h"
                )
            )
        }
    }
}
