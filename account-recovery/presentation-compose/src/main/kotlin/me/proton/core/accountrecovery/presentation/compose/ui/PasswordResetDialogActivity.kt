/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.accountrecovery.presentation.compose.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogInput
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.errorToast
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.successToast

@AndroidEntryPoint
class PasswordResetDialogActivity : ProtonActivity() {

    private val input: AccountRecoveryDialogInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val userId by lazy { UserId(input.userId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProtonTheme {
                NavHost(
                    navController = rememberNavController(),
                    startDestination = Route.Reset.Deeplink
                ) {
                    addPasswordResetDialog(
                        userId = userId,
                        onRecoveryMethod = { showRecoveryMethod() },
                        onDismiss = { finish() },
                        onError = { showError(it) },
                        onSuccess = { showSuccess() }
                    )
                }
            }
        }
    }

    private fun showRecoveryMethod() {
        openBrowserLink("https://account.proton.me/reset-password")
        finish()
    }

    private fun showError(error: Throwable?) {
        val message = error?.getUserMessage(resources)
        errorToast(message ?: getString(R.string.presentation_error_general))
        finish()
    }

    private fun showSuccess() {
        successToast(getString(R.string.account_recovery_reset_dialog_action_request_reset_success))
        finish()
    }

    companion object {

        const val ARG_INPUT = "arg.accountRecoveryDialogInput"

        fun start(context: Context, userId: UserId) =
            context.startActivity(getIntent(context, AccountRecoveryDialogInput(userId.id)))

        private fun getIntent(context: Context, input: AccountRecoveryDialogInput): Intent =
            Intent(context, PasswordResetDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ARG_INPUT, input)
            }
    }
}
