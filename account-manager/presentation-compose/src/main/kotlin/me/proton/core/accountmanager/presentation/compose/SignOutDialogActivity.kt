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

package me.proton.core.accountmanager.presentation.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.compose.entity.SignOutDialogInput
import me.proton.core.compose.theme.AppTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import javax.inject.Inject

@AndroidEntryPoint
class SignOutDialogActivity : ProtonActivity() {

    @Inject
    lateinit var appTheme: AppTheme

    @Inject
    lateinit var accountManager: AccountManager

    private val input: SignOutDialogInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val userId by lazy {
        input.userId?.let {
            UserId(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            appTheme {
                SignOutDialog(
                    onDismiss = { finish() },
                    onDisableAccount = { onDisableAccount() },
                    onRemoveAccount = { onRemoveAccount() }
                )
            }
        }
    }

    private fun onRemoveAccount() = lifecycleScope.launch {
        val userId = userId ?: accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
        accountManager.removeAccount(userId)
        finish()
    }

    private fun onDisableAccount() = lifecycleScope.launch {
        val userId = userId ?: accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
        accountManager.disableAccount(userId)
        finish()
    }

    companion object {

        const val ARG_INPUT = "arg.signOutDialogInput"

        fun start(context: Activity, userId: UserId? = null) {
            context.startActivityForResult(getIntent(context, SignOutDialogInput(userId?.id)), 0)
        }

        private fun getIntent(context: Context, input: SignOutDialogInput): Intent =
            Intent(context, SignOutDialogActivity::class.java).apply {
                putExtra(ARG_INPUT, input)
            }
    }
}
