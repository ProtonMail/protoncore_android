/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.accountmanager.presentation.compose.SignOutDialogActivity
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Route
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.addManualCodeInputScreen
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.addOriginSuccessScreen
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.addSignInIntroScreen
import me.proton.core.domain.entity.UserId

@AndroidEntryPoint
public class DeviceMigrationActivity : FragmentActivity() {
    private val userId: UserId by lazy { UserId(requireNotNull(intent.getStringExtra(Arg.KEY_USER_ID))) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content() = ProtonTheme {
        val navController = rememberNavController()
        NavHost(
            navController,
            startDestination = Route.SignInIntro.Deeplink
        ) {
            addSignInIntroScreen(
                userId = userId,
                onManualCodeInput = {
                    navController.navigate(Route.ManualCodeInput.get(userId)) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.backOrFinish() },
                onSuccess = {
                    navController.navigate(Route.OriginSuccess.get(userId)) {
                        popUpTo(Route.SignInIntro.Deeplink) { inclusive = true }
                    }
                }
            )
            addManualCodeInputScreen(
                userId = userId,
                onNavigateBack = { navController.backOrFinish() },
                onSuccess = {
                    navController.navigate(Route.OriginSuccess.get(userId)) {
                        popUpTo(Route.SignInIntro.Deeplink) { inclusive = true }
                    }
                }
            )
            addOriginSuccessScreen(
                userId = userId,
                onClose = { finish() },
                onSignOut = {
                    SignOutDialogActivity.start(this@DeviceMigrationActivity, userId = userId)
                    finish()
                }
            )
        }
    }

    private fun NavController.backOrFinish() {
        if (!popBackStack()) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
