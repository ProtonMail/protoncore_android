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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.compose.LoginTwoStepScreen
import me.proton.core.auth.presentation.compose.LoginTwoStepViewState
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.NextStep
import me.proton.core.auth.presentation.ui.LoginActivity.Companion.ARG_RESULT
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.openBrowserLink
import javax.inject.Inject

import me.proton.core.auth.presentation.compose.LoginTwoStepViewState.NextStep as ComposeNextStep

@AndroidEntryPoint
public class LoginTwoStepActivity : ProtonActivity() {

    @Inject
    lateinit var helpOptionHandler: HelpOptionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onCloseClicked() }

        setContent {
            ProtonTheme {
                LoginTwoStepScreen(
                    onCloseClicked = { onCloseClicked() },
                    onHelpClicked = { onHelpClicked() },
                    onForgotUsernameClicked = { helpOptionHandler.onForgotUsername(this) },
                    onForgotPasswordClicked = { helpOptionHandler.onForgotPassword(this) },
                    onErrorMessage = { onErrorMessage(it) },
                    onExternalAccountLoginNeeded = { onExternalAccountLoginNeeded() },
                    onExternalAccountNotSupported = { onExternalAccountNotSupported() },
                    onLoggedIn = { userId, nextStep -> onLoggedIn(userId, nextStep) },
                )
            }
        }
    }

    private fun onCloseClicked() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onHelpClicked() {
        startActivity(Intent(this, AuthHelpActivity::class.java))
    }

    private fun onErrorMessage(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onLoggedIn(userId: UserId, nexStep: LoginTwoStepViewState.NextStep) {
        when (nexStep) {
            ComposeNextStep.None -> onSuccess(userId, NextStep.None)
            ComposeNextStep.TwoPassMode -> onSuccess(userId, NextStep.TwoPassMode)
            ComposeNextStep.SecondFactor -> onSuccess(userId, NextStep.SecondFactor)
            ComposeNextStep.ChooseAddress -> onSuccess(userId, NextStep.ChooseAddress)
            ComposeNextStep.ChangePassword -> onChangePassword()
        }
    }

    private fun onSuccess(userId: UserId, nextStep: NextStep) {
        val intent = Intent().putExtra(ARG_RESULT, LoginResult(userId.id, nextStep))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onChangePassword() {
        supportFragmentManager.showPasswordChangeDialog(context = this)
    }

    private fun onExternalAccountLoginNeeded() {
        // Launch SSO login
    }

    private fun onExternalAccountNotSupported() {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.auth_login_external_account_unsupported_title)
            .setMessage(R.string.auth_login_external_account_unsupported_message)
            .setPositiveButton(R.string.auth_login_external_account_unsupported_help_action) { _, _ ->
                openBrowserLink(getString(R.string.external_account_help_link))
            }
            .setNegativeButton(R.string.presentation_alert_cancel, null)
            .show()
    }
}
