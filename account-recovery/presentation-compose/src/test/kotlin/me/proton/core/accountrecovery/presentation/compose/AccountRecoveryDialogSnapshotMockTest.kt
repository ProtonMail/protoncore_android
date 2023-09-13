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
import androidx.lifecycle.SavedStateHandle
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModel
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.user.domain.usecase.GetUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant

class AccountRecoveryDialogSnapshotMockTest {
    private val testUserEmail = "user@email.test"
    private val testUserId = UserId("test-user-id")

    private lateinit var clockValue: Instant

    private lateinit var clock: Clock

    private lateinit var savedStateHandle: SavedStateHandle

    @MockK(relaxed = true)
    private lateinit var observeUserRecovery: ObserveUserRecovery

    @MockK(relaxed = true)
    private lateinit var cancelRecovery: CancelRecovery

    @MockK
    private lateinit var getUser: GetUser

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    @MockK
    private lateinit var observabilityManager: ObservabilityManager

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    private lateinit var viewModel: AccountRecoveryViewModel

    @Before
    fun beforeEveryTest() {
        clockValue = Instant.now()
        clock = mockk {
            every { instant() } returns clockValue
        }
        savedStateHandle = mockk {
            every { this@mockk.get<String>(Arg.UserId) } returns testUserId.id
        }
        MockKAnnotations.init(this)

        coEvery { getUser.invoke(any(), any()) } returns mockk {
            every { userId } returns testUserId
            every { email } returns testUserEmail
        }

        viewModel = spyk(
            AccountRecoveryViewModel(
                savedStateHandle = savedStateHandle,
                clock = clock,
                observeUserRecovery = observeUserRecovery,
                cancelRecovery = cancelRecovery,
                keyStoreCrypto = keyStoreCrypto,
                manager = observabilityManager,
                getUser = getUser
            )
        )
    }

    @Test
    fun baseAccountRecoveryDialogOnLoadingTest() {
        every { viewModel.state } returns MutableStateFlow(AccountRecoveryViewModel.State.Loading).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogGracePeriodTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted(
                email = "user@email.test",
                remainingHours = 24
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
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
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryEndedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.RecoveryEnded(email = "user@email.test")
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted(
                endDate = "16 Aug"
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogPasswordChangeWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted(
                endDate = "16 Aug"
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryStateClosedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Closed()
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnErrorTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Error(throwable = Throwable("test error message"))
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.CancelPasswordReset(processing = true)
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressErrorOccurredTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Error(throwable = Throwable("test error message"))
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
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
                viewModel = viewModel,
                onClosed = {},
                onError = {}
            )
        }
    }
}
