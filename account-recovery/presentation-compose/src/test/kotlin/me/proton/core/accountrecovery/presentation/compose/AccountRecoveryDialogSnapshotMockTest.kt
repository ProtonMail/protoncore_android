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

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveAccountRecoveryState
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModel
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountRecoveryDialogSnapshotMockTest {

    private val observeAccountRecoveryState = mockk<ObserveAccountRecoveryState>(relaxed = true)
    private val cancelRecovery = mockk<CancelRecovery>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>()

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    private val testUserId = UserId("test-user-id")
    private lateinit var viewModel: AccountRecoveryViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = spyk(AccountRecoveryViewModel(observeAccountRecoveryState, cancelRecovery, keyStoreCrypto))
    }

    @Test
    fun baseAccountRecoveryDialogOnLoadingTest() {
        every { viewModel.state } returns MutableStateFlow(AccountRecoveryViewModel.State.Loading).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogGracePeriodTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted()
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogCancellationTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.CancellationHappened
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryEndedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.RecoveryEnded
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogPasswordChangeWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryStateClosedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Closed
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnErrorTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Error(message = "test error message")
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted(processing = true)
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressErrorOccurredTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Error(message = "test error message")
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogWithPaddingModifier() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.CancellationHappened
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                modifier = Modifier.padding(end = ProtonDimens.DefaultSpacing),
                userId = testUserId,
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }
}
