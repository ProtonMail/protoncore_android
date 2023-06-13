/*
 * Copyright (c) 2022 Proton Technologies AG
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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModel
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountRecoveryDialogTest {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val testUserId = UserId("test-user-id")
    private val viewModel = mockk<AccountRecoveryViewModel>(relaxed = true)

    @Test
    fun gracePeriodUIDisplaysAllElements() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_grace_started_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted(false)
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertExists()
        composeTestRule.onNodeWithText(txtTitle).assertExists()
    }

    @Test
    fun gracePeriodUIOnErrorStateDisplaysFine() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_grace_started_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Error(message = "error occurred")
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
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
        val idTitle = R.string.account_recovery_grace_started_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(AccountRecoveryViewModel.State.Loading).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
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
        val idOk = R.string.presentation_alert_ok
        var btnOk = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted()
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }

    @Test
    fun gracePeriodCancelWorksProperly() {
        // GIVEN
        val idCancelRecovery = R.string.account_recovery_cancel
        var btnCancelRecovery = ""

        every { viewModel.startAccountRecoveryCancel() } returns mockk()
        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted()
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnCancelRecovery = stringResource(id = idCancelRecovery)

            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnCancelRecovery).performClick()
        verify { viewModel.startAccountRecoveryCancel() }
    }

    @Test
    fun cancellationHappenedUI() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_cancelled_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.CancellationHappened
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertExists()
    }

    @Test
    fun cancellationHappenedOkWorksProperly() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        var btnOk = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.CancellationHappened
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }

    @Test
    fun passwordPeriodStartedDialogUI() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_password_started_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertExists()
    }

    @Test
    fun passwordPeriodStartedDialogOkWorksProperly() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        var btnOk = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }

    @Test
    fun recoveryWindowEndedDialogUI() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        val idCancelRecovery = R.string.account_recovery_cancel
        val idTitle = R.string.account_recovery_window_ended_title
        var btnOk = ""
        var btnCancelRecovery = ""
        var txtTitle = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.RecoveryEnded
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            btnCancelRecovery = stringResource(id = idCancelRecovery)
            txtTitle = stringResource(id = idTitle)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).assertExists()
        composeTestRule.onNodeWithText(btnCancelRecovery).assertDoesNotExist()
        composeTestRule.onNodeWithText(txtTitle).assertExists()
    }

    @Test
    fun recoveryWindowDialogEndedDialogButtonOkWorksProperly() {
        // GIVEN
        val idOk = R.string.presentation_alert_ok
        var btnOk = ""

        every { viewModel.state } returns MutableStateFlow(
            AccountRecoveryViewModel.State.Opened.RecoveryEnded
        ).asStateFlow()

        // WHEN
        composeTestRule.setContent {
            btnOk = stringResource(id = idOk)
            ProtonTheme {
                AccountRecoveryDialog(userId = testUserId, viewModel = viewModel, onClosed = {}, onError = {})
            }
        }

        // THEN
        composeTestRule.onNodeWithText(btnOk).performClick()
        verify { viewModel.userAcknowledged() }
    }
}