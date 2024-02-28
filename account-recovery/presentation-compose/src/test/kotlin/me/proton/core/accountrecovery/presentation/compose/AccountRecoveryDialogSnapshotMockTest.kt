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
import app.cash.paparazzi.detectEnvironment
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryDialogViewModel
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.usecase.GetUser
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountRecoveryDialogSnapshotMockTest {
    private val testUserEmail = "user@email.test"
    private val testUserId = UserId("test-user-id")
    private val testSessionId = SessionId("test-session-id")

    private val testSession = Session.Authenticated(
        testUserId,
        testSessionId,
        "test-access-token",
        "test-refresh-token",
        emptyList()
    )

    private var clockValue: Long = 1709828123000

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

    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var userManager: UserManager

    @MockK
    private lateinit var isAccountRecoveryResetEnabled: IsAccountRecoveryResetEnabled

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme",
        // Remove when layoutlib properly supports SDK 34 (https://github.com/cashapp/paparazzi/issues/1025).
        environment = detectEnvironment().run {
            copy(compileSdkVersion = 33, platformDir = platformDir.replace("34", "33"))
        }
    )

    private lateinit var viewModel: AccountRecoveryDialogViewModel

    @Before
    fun beforeEveryTest() {
        clock = Clock.fixed(clockValue)

        savedStateHandle = mockk {
            every { this@mockk.get<String>(Arg.UserId) } returns testUserId.id
        }
        MockKAnnotations.init(this)

        coEvery { getUser.invoke(any(), any()) } returns mockk {
            every { userId } returns testUserId
            every { email } returns testUserEmail
        }

        coEvery { accountManager.getSessions() } returns flowOf(
            listOf(testSession)
        )

        coEvery { userManager.observeUser(any(), any()) } returns flowOf(
            mockk {
                every { userId } returns testUserId
                every { email } returns testUserEmail
                every { recovery } returns mockk {
                    every { sessionId } returns SessionId("test-different-session-id")
                }
            }
        )

        viewModel = spyk(
            AccountRecoveryDialogViewModel(
                savedStateHandle = savedStateHandle,
                clock = clock,
                dateTimeFormat = DateTimeFormat(paparazzi.context),
                observeUserRecovery = observeUserRecovery,
                cancelRecovery = cancelRecovery,
                keyStoreCrypto = keyStoreCrypto,
                observabilityManager = observabilityManager,
                getUser = getUser,
                accountManager = accountManager,
                userManager = userManager,
                isAccountRecoveryResetEnabled = isAccountRecoveryResetEnabled
            )
        )
    }

    @Test
    fun baseAccountRecoveryDialogOnLoadingTest() {
        every { viewModel.state } returns MutableStateFlow(AccountRecoveryDialogViewModel.State.Loading).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogGracePeriodTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.GracePeriodStarted(
                email = "user@email.test",
                remainingHours = 24
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogCancellationTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.CancellationHappened
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryEndedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.RecoveryEnded(email = "user@email.test")
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogRecoveryWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
                endDate = "16 Aug"
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogPasswordChangeWindowTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
                endDate = "16 Aug"
            )
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryStateClosedTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Closed()
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnErrorTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Error(throwable = Throwable("test error message"))
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.CancelPasswordReset(processing = true)
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryOnCancellationProgressErrorOccurredTest() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Error(throwable = Throwable("test error message"))
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }

    @Test
    fun baseAccountRecoveryDialogWithPaddingModifier() {
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryDialogViewModel.State.Opened.CancellationHappened
        ).asStateFlow()
        paparazzi.snapshot {
            AccountRecoveryDialog(
                modifier = Modifier.padding(end = ProtonDimens.DefaultSpacing),
                viewModel = viewModel,
                onClosed = {},
                onError = {},
                onStartPasswordManager = {}
            )
        }
    }
}
