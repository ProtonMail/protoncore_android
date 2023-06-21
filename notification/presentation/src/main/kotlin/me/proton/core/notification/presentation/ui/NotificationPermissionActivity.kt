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

package me.proton.core.notification.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.notification.presentation.R
import me.proton.core.notification.presentation.viewmodel.NotificationPermissionViewModel
import me.proton.core.presentation.ui.ProtonActivity

@AndroidEntryPoint
public class NotificationPermissionActivity : ProtonActivity() {
    private var alertDialog: AlertDialog? = null
    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) {
        viewModel.onNotificationPermissionRequestResult(isGranted = it)
    }
    private val viewModel by viewModels<NotificationPermissionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.onEach(this::handleState).launchIn(lifecycleScope)
        viewModel.setup(this)
    }

    override fun onDestroy() {
        alertDialog?.dismiss()
        super.onDestroy()
    }

    private fun handleState(state: NotificationPermissionViewModel.State) = when (state) {
        NotificationPermissionViewModel.State.Idle -> Unit
        NotificationPermissionViewModel.State.Finish -> finish()
        NotificationPermissionViewModel.State.ShowRationale -> showNotificationPermissionRationale()
        NotificationPermissionViewModel.State.LaunchPermissionRequest -> launchPermissionRequest()
    }

    @SuppressLint("InlinedApi")
    private fun launchPermissionRequest() =
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

    private fun showNotificationPermissionRationale() {
        alertDialog?.dismiss()
        alertDialog = MaterialAlertDialogBuilder(this)
            .setMessage(R.string.core_notification_rationale)
            .setPositiveButton(R.string.presentation_alert_continue) { _, _ ->
                launchPermissionRequest()
            }
            .setNegativeButton(R.string.presentation_alert_cancel) { _, _ ->
                finish()
            }
            .setOnDismissListener { finish() }
            .setCancelable(true)
            .show()
    }

    internal companion object {
        operator fun invoke(context: Context): Intent =
            Intent(context, NotificationPermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
    }
}
