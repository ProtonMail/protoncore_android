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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.devicemigration.presentation.TargetDeviceMigrationRoutes.addSignInScreen
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import javax.inject.Inject

@AndroidEntryPoint
public class TargetDeviceMigrationActivity : ProtonActivity() {
    @Inject
    internal lateinit var observabilityManager: ObservabilityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    @Composable
    private fun Content(navController: NavHostController = rememberNavController()) = ProtonTheme {
        NavHost(
            navController,
            startDestination = TargetDeviceMigrationRoutes.Route.SignIn.Deeplink,
            modifier = Modifier.safeDrawingPadding()
        ) {
            addSignInScreen(
                observabilityManager = observabilityManager,
                onBackToSignIn = {
                    setResult(RESULT_CANCELED, Intent().apply {
                        putExtra(ARG_RESULT, TargetDeviceMigrationResult.NavigateToSignIn)
                    })
                    finish()
                },
                onNavigateBack = { finish() },
                onSuccess = { userId: UserId ->
                    onSuccess(userId, shouldChangePassword = false)
                },
                onSuccessAndPasswordChange = { userId: UserId ->
                    onSuccess(userId, shouldChangePassword = true)
                },
            )
        }
    }

    private fun onSuccess(userId: UserId, shouldChangePassword: Boolean) {
        val result = when {
            shouldChangePassword -> TargetDeviceMigrationResult.PasswordChangeNeeded
            else -> TargetDeviceMigrationResult.SignedIn(userId.id)
        }
        setResult(RESULT_OK, Intent().apply {
            putExtra(ARG_RESULT, result)
        })
        finish()
    }

    internal companion object {
        const val ARG_RESULT = "result"
    }
}
