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

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.proton.core.auth.presentation.compose.confirmationcode.SignInRequestedForApprovalScreen
import me.proton.core.domain.entity.UserId

internal object Arg {
    const val MemberId = "MemberId"
    const val UserId = "UserId"
}

internal object Route {
    object AdminApproval {
        const val Deeplink = "auth/{${Arg.UserId}}/member/${Arg.MemberId}/device/approval"
        fun get(userId: UserId, memberId: UserId) = "auth/${userId.id}/member/${memberId.id}/device/approval"
    }

    object SelfApproval {
        const val Deeplink = "auth/{${Arg.UserId}}/device/approval"
        fun get(userId: UserId) = "auth/${userId.id}/device/approval"
    }
}

internal fun NavGraphBuilder.addSelfApprovalScreen(
    userId: UserId,
    onClose: () -> Unit,
    onError: (String?) -> Unit
) {
    composable(
        route = Route.SelfApproval.Deeplink,
        arguments = listOf(
            navArgument(Arg.UserId) {
                type = NavType.StringType
                defaultValue = userId.id
            }
        ),
    ) {
        SignInRequestedForApprovalScreen(
            onClose = onClose,
            onErrorMessage = onError
        )
    }
}

internal fun NavGraphBuilder.addAdminApprovalScreen(
    userId: UserId
) {
    composable(
        route = Route.AdminApproval.Deeplink,
        arguments = listOf(
            navArgument(Arg.MemberId) { type = NavType.StringType },
            navArgument(Arg.UserId) {
                type = NavType.StringType
                defaultValue = userId.id
            }
        ),
    ) {
        // TODO admin approval screen
    }
}
