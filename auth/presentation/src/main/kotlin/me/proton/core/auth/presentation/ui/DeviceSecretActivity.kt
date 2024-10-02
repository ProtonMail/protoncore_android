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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.compose.DeviceSecretAction
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.addRequestAdminHelpScreen
import me.proton.core.auth.presentation.compose.DeviceSecretViewModel
import me.proton.core.auth.presentation.entity.DeviceSecretResult
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.addBackupPasswordInputScreen
import me.proton.core.auth.presentation.compose.DeviceSecretRoutes.addMainScreen
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorToast

@AndroidEntryPoint
class DeviceSecretActivity : ProtonActivity() {

    private val userId: UserId by lazy {
        UserId(requireNotNull(intent.getStringExtra(ARG_INPUT)))
    }

    private val viewModel by viewModels<DeviceSecretViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { viewModel.submit(DeviceSecretAction.Close) }

        setContent {
            ProtonTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = DeviceSecretRoutes.Route.Main.get(userId)
                ) {
                    addMainScreen(
                        userId = userId,
                        navController = navController,
                        onClose = { onClose() },
                        onErrorMessage = { onErrorMessage(it) },
                        onSuccess = { onSuccess(it) }
                    )
                    addBackupPasswordInputScreen(
                        userId = userId,
                        navController = navController,
                        onErrorMessage = { onErrorMessage(it) },
                    )
                    addRequestAdminHelpScreen(
                        userId = userId,
                        navController = navController,
                        onErrorMessage = { onErrorMessage(it) }
                    )
                }
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onErrorMessage(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess(userId: UserId) {
        val intent = Intent().putExtra(ARG_RESULT, DeviceSecretResult(userId.id))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.userId"
        const val ARG_RESULT = "arg.result"
    }
}
