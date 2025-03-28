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
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity.RESULT_OK
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.proton.core.devicemigration.presentation.DeviceMigrationActivity.Companion.ARG_SHOULD_LOG_OUT
import me.proton.core.devicemigration.presentation.DeviceMigrationActivity.Companion.ARG_USER_ID
import me.proton.core.devicemigration.presentation.codeinput.ManualCodeInputScreen
import me.proton.core.devicemigration.presentation.intro.SignInIntroScreen
import me.proton.core.devicemigration.presentation.success.OriginSuccessScreen
import me.proton.core.domain.entity.UserId

internal object DeviceMigrationRoutes {
    internal object Arg {
        const val KEY_USER_ID = "userId"

        fun SavedStateHandle.getUserId(): UserId = UserId(
            checkNotNull(get<String>(KEY_USER_ID)) { "Missing '$KEY_USER_ID' key in SavedStateHandle" }
        )
    }

    object Route {
        object SignInIntro {
            const val Deeplink: String =
                "device_migration/{${Arg.KEY_USER_ID}}/origin/intro"

            fun get(userId: UserId): String =
                "device_migration/${userId.id}/origin/intro"
        }

        object ManualCodeInput {
            const val Deeplink: String =
                "device_migration/{${Arg.KEY_USER_ID}}/origin/code_input"

            fun get(userId: UserId): String =
                "device_migration/${userId.id}/origin/code_input"
        }

        object OriginSuccess {
            const val Deeplink: String =
                "device_migration/{${Arg.KEY_USER_ID}}/origin/success"

            fun get(userId: UserId): String =
                "device_migration/${userId.id}/origin/success"
        }
    }

    fun NavGraphBuilder.addSignInIntroScreen(
        userId: UserId,
        onManualCodeInput: () -> Unit = {},
        onNavigateBack: () -> Unit = {},
        onSuccess: () -> Unit = {},
    ) {
        composable(
            route = Route.SignInIntro.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            )
        ) {
            SignInIntroScreen(
                onManualCodeInput = onManualCodeInput,
                onNavigateBack = onNavigateBack,
                onSuccess = onSuccess
            )
        }
    }

    fun NavGraphBuilder.addManualCodeInputScreen(
        userId: UserId,
        onNavigateBack: () -> Unit = {},
        onSuccess: () -> Unit = {},
    ) {
        composable(
            route = Route.ManualCodeInput.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
            enterTransition = { EnterTransition.None },
        ) {
            ManualCodeInputScreen(
                onNavigateBack = onNavigateBack,
                onSuccess = onSuccess
            )
        }
    }

    fun NavGraphBuilder.addOriginSuccessScreen(
        userId: UserId,
        onClose: () -> Unit,
        onSignOut: () -> Unit,
    ) {
        composable(
            route = Route.OriginSuccess.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                })
        ) {
            val activity = LocalActivity.current

            LaunchedEffect(activity) {
                activity?.setResult(RESULT_OK, Intent().apply {
                    putExtra(ARG_USER_ID, userId.id)
                })
            }

            OriginSuccessScreen(
                onClose = onClose,
                onSignOut = {
                    activity?.setResult(RESULT_OK, Intent().apply {
                        putExtra(ARG_SHOULD_LOG_OUT, true)
                        putExtra(ARG_USER_ID, userId.id)
                    })
                    onSignOut()
                }
            )
        }
    }
}
