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
import me.proton.core.accountmanager.presentation.AccountManagerOrchestrator
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogInput
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.errorToast
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.successToast
import javax.inject.Inject

@AndroidEntryPoint
class AccountRecoveryDialogActivity : ProtonActivity() {

    @Inject
    lateinit var accountManagerOrchestrator: AccountManagerOrchestrator

    private val input: AccountRecoveryDialogInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val userId by lazy { UserId(input.userId) }

    override fun onDestroy() {
        accountManagerOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountManagerOrchestrator.register(this)

        setContent {
            ProtonTheme {
                NavHost(
                    navController = rememberNavController(),
                    startDestination = Route.Recovery.Deeplink
                ) {
                    addAccountRecoveryDialog(
                        userId = userId,
                        onClosed = { close(it) },
                        onError = { showErrorAndFinish(it) },
                        onStartPasswordManager = { startPasswordManager(it) }
                    )
                }
            }
        }
    }

    private fun startPasswordManager(userId: UserId) {
        accountManagerOrchestrator.startPasswordManagementWorkflow(userId)
        close()
    }

    private fun showErrorAndFinish(error: Throwable?) {
        val message = error?.getUserMessage(resources)
        errorToast(message ?: getString(R.string.presentation_error_general))
        close()
    }

    private fun close(hasCancelledSuccessfully: Boolean = false) {
        if (hasCancelledSuccessfully) {
            successToast(getString(R.string.account_recovery_cancelled_title))
        }
        finish()
    }

    companion object {

        const val ARG_INPUT = "arg.accountRecoveryDialogInput"

        fun start(context: Context, input: AccountRecoveryDialogInput) =
            context.startActivity(getIntent(context, input))

        fun getIntent(context: Context, input: AccountRecoveryDialogInput): Intent =
            Intent(context, AccountRecoveryDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ARG_INPUT, input)
            }
    }
}
