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
import me.proton.core.auth.presentation.compose.sso.backuppassword.input.BackupPasswordInputScreen
import me.proton.core.domain.entity.UserId

public object DeviceSecretRoutes {

    internal object Arg {
        const val KEY_USER_ID = "userId"

        fun SavedStateHandle.getUserId(): UserId = UserId(
            checkNotNull(get<String>(KEY_USER_ID)) { "Missing '$KEY_USER_ID' key in SavedStateHandle" }
        )
    }

    public object Route {
        public object Main {
            public const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/device/secret"
            public fun get(userId: UserId): String = "auth/${userId.id}/device/secret"
        }

        public object EnterBackupPassword {
            public const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/device/secret/password"
            public fun get(userId: UserId): String = "auth/${userId.id}/device/secret/password"
        }

        public object AskAdminHelp {
            public const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/device/secret/admin"
            public fun get(userId: UserId): String = "auth/${userId.id}/device/secret/admin"
        }
    }

    public fun NavGraphBuilder.addMainScreen(
        userId: UserId,
        navController: NavHostController,
        onClose: () -> Unit,
        onError: (String?) -> Unit,
        onSuccess: (userId: UserId) -> Unit
    ) {
        composable(
            route = Route.Main.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
        ) {
            DeviceSecretScreen(
                onClose = { onClose() },
                onError = { onError(it) },
                onSuccess = { onSuccess(it) },
                onNavigateToEnterBackupPassword = {
                    navController.navigate(Route.EnterBackupPassword.get(userId))
                }
            )
        }
    }

    public fun NavGraphBuilder.addEnterBackupPasswordScreen(
        userId: UserId,
        navController: NavHostController,
        onError: (String?) -> Unit
    ) {
        composable(
            route = Route.EnterBackupPassword.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
        ) {
            BackupPasswordInputScreen(
                onAskAdminHelpClicked = { navController.navigate(Route.AskAdminHelp.get(userId)) },
                onCloseClicked = { navController.popBackStack() },
                onError = { onError(it) },
                onSuccess = { navController.popBackStack() }
            )
        }
    }

    internal fun NavGraphBuilder.addAskAdminHelpScreen(
        userId: UserId,
        navController: NavHostController,
        onError: (String?) -> Unit
    ) {
        composable(
            route = Route.AskAdminHelp.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
        ) {
            TODO()
        }
    }
}
