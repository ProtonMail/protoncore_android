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

package me.proton.core.accountrecovery.presentation.compose.dialog

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryDialogViewModel
import me.proton.core.accountrecovery.presentation.compose.viewmodel.toScreenId
import me.proton.core.compose.component.PROTON_OUTLINED_TEXT_INPUT_TAG
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.observability.domain.metrics.AccountRecoveryScreenViewTotal
import me.proton.core.presentation.utils.StringBox
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountRecoveryDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val passwordInput: SemanticsNodeInteraction
        get() = composeTestRule.onNodeWithTag(PASSWORD_FIELD_TAG)
            .onChildren()
            .filterToOne(hasTestTag(PROTON_OUTLINED_TEXT_INPUT_TAG))

    private val viewModel = mockk<AccountRecoveryDialogViewModel>(relaxed = true)

    @Test
    fun gracePeriodUIDisplaysAllElements() {
        // GIVEN
        val idOk = R.string.account_recovery_dismiss
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_grace_period_info_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.GracePeriodStarted(
                email = "user@email.test",
                remainingHours = 24
            )
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertExists()
        composeTestRule.onNodeWithText(txtTitle).assertExists()

        verify {
            viewModel.onScreenView(AccountRecoveryScreenViewTotal.ScreenId.gracePeriodInfo)
        }
    }

    @Test
    fun gracePeriodUIOnErrorStateDisplaysFine() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_grace_period_info_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Error(throwable = Throwable("error occurred"))
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertDoesNotExist()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertDoesNotExist()
    }

    @Test
    fun gracePeriodUIOnProcessingStateDisplaysFine() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_grace_period_info_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Loading
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertDoesNotExist()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertDoesNotExist()
        composeTestRule.onNodeWithTag("progressIndicator").assertExists()
    }

    @Test
    fun gracePeriodOkWorksProperly() {
        // GIVEN
        val idOk = R.string.account_recovery_dismiss
        var btnOk = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.GracePeriodStarted(
            email = "user@email.test",
            remainingHours = 24
        )
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }

    @Test
    fun gracePeriodCancelWorksProperly() {
        // GIVEN
        val idCancelRecovery = R.string.account_recovery_cancel_now
        var btnCancelRecovery = ""
        val state = AccountRecoveryDialogViewModel.State.Opened.CancelPasswordReset(
            onCancelPasswordRequest = { viewModel.startAccountRecoveryCancel(it) }
        )

        every { viewModel.startAccountRecoveryCancel("password") } returns mockk()
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnCancelRecovery = stringResource(id = idCancelRecovery)

            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        passwordInput.performTextInput("password")
        composeTestRule.onNodeWithText(btnCancelRecovery).performClick()

        // THEN
        verify { viewModel.startAccountRecoveryCancel("password") }
    }

    @Test
    fun gracePeriodInvalidPassword() {
        // GIVEN
        val idCancelRecovery = R.string.account_recovery_cancel_now
        var btnCancelRecovery = ""
        val state = AccountRecoveryDialogViewModel.State.Opened.CancelPasswordReset(
            onCancelPasswordRequest = { viewModel.startAccountRecoveryCancel(it) }
        )
        val stateFlow = MutableStateFlow(state)

        every { viewModel.startAccountRecoveryCancel(any()) } coAnswers {
            stateFlow.value =
                AccountRecoveryDialogViewModel.State.Opened.CancelPasswordReset(
                    passwordError = StringBox("Password cannot be empty")
                )
            mockk()
        }
        every { viewModel.state } returns stateFlow.asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnCancelRecovery = stringResource(id = idCancelRecovery)

            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }
        passwordInput.performTextInput("invalid")
        composeTestRule.onNodeWithText(btnCancelRecovery).performClick()

        // THEN
        verify { viewModel.startAccountRecoveryCancel("invalid") }
        passwordInput.assertIsEnabled()
    }

    @Test
    fun cancellationHappenedUI() {
        // GIVEN
        val idOk = R.string.presentation_close
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_cancelled_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.CancellationHappened
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertExists()

        verify {
            viewModel.onScreenView(AccountRecoveryScreenViewTotal.ScreenId.recoveryCancelledInfo)
        }
    }

    @Test
    fun cancellationHappenedOkWorksProperly() {
        // GIVEN
        val idOk = R.string.presentation_close
        var btnOk = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.CancellationHappened
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }

    @Test
    fun passwordPeriodStartedDialogUI() {
        // GIVEN
        val gotIt = R.string.account_recovery_dismiss
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_password_started_title
        var btnGotIt = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
            endDate = "16 Aug"
        )
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnGotIt = stringResource(id = gotIt)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnGotIt).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertExists()
        composeTestRule.onNodeWithText(txtTitle).assertExists()

        verify {
            viewModel.onScreenView(AccountRecoveryScreenViewTotal.ScreenId.passwordChangeInfo)
        }
    }

    @Test
    fun passwordPeriodStartedDialogOkWorksProperly() {
        // GIVEN
        val idOk = R.string.account_recovery_cancel
        var btnOk = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.PasswordChangePeriodStarted.OtherDeviceInitiated(
            endDate = "16 Aug",
            onShowCancellationForm = { viewModel.showCancellationForm() }
        )
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.showCancellationForm() }
    }

    @Test
    fun recoveryWindowEndedDialogUI() {
        // GIVEN
        val idOk = R.string.account_recovery_dismiss
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_window_ended_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.RecoveryEnded(email = "user@email.test")
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertExists()

        verify {
            viewModel.onScreenView(AccountRecoveryScreenViewTotal.ScreenId.recoveryExpiredInfo)
        }
    }

    @Test
    fun recoveryWindowDialogEndedDialogButtonOkWorksProperly() {
        // GIVEN
        val idOk = R.string.account_recovery_dismiss
        var btnOk = ""

        val state = AccountRecoveryDialogViewModel.State.Opened.RecoveryEnded(email = "user@email.test")
        every { viewModel.state } returns MutableStateFlow(state).asStateFlow()
        every { viewModel.screenId } returns MutableStateFlow(state.toScreenId())

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(
                    viewModel = viewModel,
                    onStartPasswordManager = {},
                    onClosed = {},
                    onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }
}