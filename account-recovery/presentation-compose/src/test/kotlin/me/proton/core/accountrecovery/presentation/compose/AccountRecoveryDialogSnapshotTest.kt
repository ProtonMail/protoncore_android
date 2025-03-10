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

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryCancellationForm
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryCancelledDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryGracePeriodDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryPasswordPeriodStartedDialog
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryWindowEndedDialog
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryDialogViewModel.State
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryDialogViewModel.State.Opened.PasswordChangePeriodStarted
import me.proton.core.presentation.utils.StringBox
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
                email = "user@email.test",
                remainingHours = 24
            )
        }
    }

    @Test
    fun accountRecoveryInvalidPasswordTest() {
        paparazzi.snapshot {
            AccountRecoveryCancellationForm(
                passwordError = StringBox("Invalid password")
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
            AccountRecoveryPasswordPeriodStartedDialog(
                endDate = "16 Aug"
            )
        }
    }

    @Test
    fun accountRecoveryWindowEndingTest() {
        paparazzi.snapshot {
            AccountRecoveryWindowEndedDialog(email = "user@email.test") { }
        }
    }

    @Test
    fun accountRecoveryStateErrorTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Error(Throwable("test"))
            )
        }
    }

    @Test
    fun accountRecoveryStateLoadingTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Loading
            )
        }
    }

    @Test
    fun accountRecoveryStateClosedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Closed()
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedRecoveryEndedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Opened.RecoveryEnded(email = "user@email.test")
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedCancellationHappenedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Opened.CancellationHappened
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedPasswordChangeStartedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = PasswordChangePeriodStarted.OtherDeviceInitiated(
                    endDate = "16 Aug"
                )
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedPasswordChangeStartedSelfInitiatedTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = PasswordChangePeriodStarted.SelfInitiated(
                    endDate = "16 Aug"
                )
            )
        }
    }

    @Test
    fun accountRecoveryStateOpenedGracePeriodStartedProcessingTest() {
        paparazzi.snapshot {
            AccountRecoveryDialog(
                state = State.Opened.CancelPasswordReset(
                    processing = true
                )
            )
        }
    }
}
