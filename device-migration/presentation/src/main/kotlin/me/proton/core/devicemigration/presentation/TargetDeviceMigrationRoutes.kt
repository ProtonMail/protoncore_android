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

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.proton.core.compose.util.LaunchOnScreenView
import me.proton.core.devicemigration.presentation.signin.SignInScreen
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.EdmScreenViewTotal

internal object TargetDeviceMigrationRoutes {
    object Route {
        object SignIn {
            const val Deeplink: String = "device_migration/target/sign_in"
            fun get(): String = Deeplink
        }
    }

    fun NavGraphBuilder.addSignInScreen(
        observabilityManager: ObservabilityManager? = null,
        onBackToSignIn: () -> Unit = {},
        onNavigateBack: () -> Unit = {},
        onSuccess: (userId: UserId) -> Unit,
        onSuccessAndPasswordChange: (userId: UserId) -> Unit,
    ) {
        composable(
            route = Route.SignIn.Deeplink
        ) {
            LaunchOnScreenView {
                observabilityManager?.enqueue(EdmScreenViewTotal(EdmScreenViewTotal.ScreenId.target_sign_in))
            }
            SignInScreen(
                onBackToSignIn = onBackToSignIn,
                onNavigateBack = onNavigateBack,
                onSuccess = onSuccess,
                onSuccessAndPasswordChange = onSuccessAndPasswordChange,
            )
        }
    }
}
