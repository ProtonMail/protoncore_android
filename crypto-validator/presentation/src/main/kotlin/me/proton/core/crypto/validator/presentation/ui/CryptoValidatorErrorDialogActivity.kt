/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.crypto.validator.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.crypto.validator.presentation.R
import me.proton.core.crypto.validator.presentation.viewmodel.CryptoValidatorErrorViewModel
import me.proton.core.presentation.utils.openBrowserLink
import kotlin.system.exitProcess

@AndroidEntryPoint
public class CryptoValidatorErrorDialogActivity : AppCompatActivity() {

    private val viewModel by viewModels<CryptoValidatorErrorViewModel>()

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFinishOnTouchOutside(false)
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.shouldShowDialog) {
            lifecycleScope.launch {
                val hasActiveAccounts = viewModel.hasAccounts.first()
                showDialog(hasActiveAccounts)
            }
        } else {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()

        dialog?.dismiss()
        dialog = null
    }

    private fun showDialog(hasActiveAccounts: Boolean) {
        dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.crypto_keystore_error_title)
            .setMessage(R.string.crypto_keystore_error_message)
            .setPositiveButton(R.string.crypto_keystore_error_continue_action) { _, _ -> allowInsecureKeystore() }
            .setNegativeButton(R.string.crypto_keystore_error_more_info_action) { _, _ -> openHelpPage() }
            .apply {
                if (hasActiveAccounts) {
                    setNeutralButton(R.string.crypto_keystore_error_logout_action) { _, _ -> removeAllAccounts() }
                } else {
                    setNeutralButton(R.string.crypto_keystore_error_exit_action) { _, _ -> closeApp() }
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun allowInsecureKeystore() {
        viewModel.allowInsecureKeystore()
        finish()
    }

    private fun removeAllAccounts() {
        viewModel.viewModelScope.launch {
            viewModel.removeAllAccounts()
            finish()
        }
    }

    private fun closeApp() {
        // Prevents app restart
        moveTaskToBack(true)
        // Kills the app
        exitProcess(0)
    }

    private fun openHelpPage() {
        val url = getString(R.string.crypto_keystore_help_url)
        openBrowserLink(url)
    }

    public companion object {
        public fun show(context: Context) {
            val intent = Intent(context, CryptoValidatorErrorDialogActivity::class.java)
            context.startActivity(intent)
        }
    }
}
