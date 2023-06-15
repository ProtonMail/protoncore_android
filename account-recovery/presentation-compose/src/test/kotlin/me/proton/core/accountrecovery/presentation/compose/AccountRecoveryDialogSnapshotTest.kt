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

package me.proton.core.accountrecovery.presentation.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryCancelledDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryGracePeriodDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryPasswordPeriodStartedDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryWindowEndedDialog
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModel
import org.junit.Rule
import org.junit.Test

class AccountRecoveryDialogSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun accountRecoveryGracePeriodTest() {
        paparazzi.snapshot {
            AccountRecoveryGracePeriodDialog(
                onGracePeriodCancel = {},
                onDismiss = { },
                password = remember { mutableStateOf("") },
                passwordError = false,
            )
        }
    }

    @Test
    fun accountRecoveryInvalidPasswordTest() {
        paparazzi.snapshot {
            AccountRecoveryGracePeriodDialog(
                onGracePeriodCancel = {},
                onDismiss = { },
                password = remember { mutableStateOf("invalid") },
                passwordError = true,
            )
        }
    }

    @Test
    fun accountRecoveryCancellationTest() {
        paparazzi.snapshot {
            AccountRecoveryCancelledDialog { }
        }
    }

    @Test
    fun accountRecoveryPasswordPeriodTest() {
        paparazzi.snapshot {
            AccountRecoveryPasswordPeriodStartedDialog { }
        }
    }

    @Test
    fun accountRecoveryWindowEndingTest() {
        paparazzi.snapshot {
            AccountRecoveryWindowEndedDialog { }
        }
    }

    @Test
    fun accountRecoveryStateErrorTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Error(Throwable("test"))
            )
        }
    }

    @Test
    fun accountRecoveryStateLoadingTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Loading
            )
        }
    }

    @Test
    fun accountRecoveryStateClosedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Closed
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedRecoveryEndedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Opened.RecoveryEnded
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedCancellationHappenedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Opened.CancellationHappened
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedPasswordChangeStartedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedGracePeriodStartedNoProcessingTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Opened.GracePeriodStarted()
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedGracePeriodStartedProcessingTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = AccountRecoveryViewModel.State.Opened.GracePeriodStarted(processing = true)
            )
        }
    }
}
