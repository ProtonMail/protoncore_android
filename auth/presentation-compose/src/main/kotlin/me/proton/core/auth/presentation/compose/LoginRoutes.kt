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

package me.proton.core.auth.presentation.compose

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.domain.entity.UserId

public object LoginRoutes {

    internal object Arg {
        const val KEY_USERNAME = "username"
        fun SavedStateHandle.getUsername(): String? = get<String>(KEY_USERNAME)
        fun SavedStateHandle.setUsername(username: String) = set(KEY_USERNAME, username)
    }

    public object Route {
        public object Login {
            public const val Deeplink: String = "auth/{${Arg.KEY_USERNAME}}/login"
            public fun get(username: String): String = "auth/$username/login"
        }

        public object Srp {
            public const val Deeplink: String = "auth/{${Arg.KEY_USERNAME}}/login/srp"
            public fun get(username: String): String = "auth/$username/login/srp"
        }
    }

    public fun NavGraphBuilder.addLoginInputUsernameScreen(
        username: String?,
        navController: NavHostController,
        onClose: () -> Unit = {},
        onErrorMessage: (String?, UserCheckAction?) -> Unit = { _, _ -> },
        onSuccess: (userId: UserId) -> Unit = {},
        onNavigateToHelp: () -> Unit = {},
        onNavigateToSso: (AuthInfo.Sso) -> Unit = {},
        onNavigateToForgotUsername: () -> Unit = {},
        onNavigateToTroubleshoot: () -> Unit = {},
        onNavigateToExternalEmailNotSupported: () -> Unit = {},
        onNavigateToExternalSsoNotSupported: () -> Unit = {},
        onNavigateToChangePassword: () -> Unit = {},
        externalAction: SharedFlow<LoginInputUsernameAction> = MutableSharedFlow(),
    ) {
        composable(
            route = Route.Login.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USERNAME) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            LoginInputUsernameScreen(
                initialUsername = username,
                onClose = { onClose() },
                onErrorMessage = { message, action -> onErrorMessage(message, action) },
                onSuccess = { onSuccess(it) },
                onNavigateToHelp = { onNavigateToHelp() },
                onNavigateToSrp = { navController.navigate(Route.Srp.get(it.username)) },
                onNavigateToSso = { onNavigateToSso(it) },
                onNavigateToForgotUsername = { onNavigateToForgotUsername() },
                onNavigateToTroubleshoot = { onNavigateToTroubleshoot() },
                onNavigateToExternalEmailNotSupported = { onNavigateToExternalEmailNotSupported() },
                onNavigateToExternalSsoNotSupported = { onNavigateToExternalSsoNotSupported() },
                onNavigateToChangePassword = { onNavigateToChangePassword() },
                externalAction = externalAction
            )
        }
    }

    public fun NavGraphBuilder.addLoginInputPasswordScreen(
        navController: NavHostController,
        onErrorMessage: (String?, UserCheckAction?) -> Unit = { _, _ -> },
        onSuccess: (userId: UserId) -> Unit = {},
        onNavigateToHelp: () -> Unit = {},
        onNavigateToForgotPassword: () -> Unit = {},
        onNavigateToTroubleshoot: () -> Unit = {},
        onNavigateToExternalEmailNotSupported: () -> Unit = {},
        onNavigateToExternalSsoNotSupported: () -> Unit = {},
        onNavigateToChangePassword: () -> Unit = {}
    ) {
        composable(
            route = Route.Srp.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USERNAME) {
                    type = NavType.StringType
                }
            )
        ) {
            LoginInputPasswordScreen(
                onClose = { navController.popBackStack() },
                onErrorMessage = { message, action -> onErrorMessage(message, action) },
                onSuccess = { onSuccess(it) },
                onNavigateToHelp = { onNavigateToHelp() },
                onNavigateToSrp = { navController.popBackStack() },
                onNavigateToSso = { navController.popBackStack() },
                onNavigateToForgotPassword = { onNavigateToForgotPassword() },
                onNavigateToTroubleshoot = { onNavigateToTroubleshoot() },
                onNavigateToExternalEmailNotSupported = { onNavigateToExternalEmailNotSupported() },
                onNavigateToExternalSsoNotSupported = { onNavigateToExternalSsoNotSupported() },
                onNavigateToChangePassword = { onNavigateToChangePassword() }
            )
        }
    }
}
