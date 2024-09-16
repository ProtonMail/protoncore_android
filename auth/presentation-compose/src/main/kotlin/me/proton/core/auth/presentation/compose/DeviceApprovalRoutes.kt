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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.auth.presentation.compose.confirmationcode.SignInRequestedForApprovalScreen
import me.proton.core.domain.entity.UserId

public object DeviceApprovalRoutes {

    internal object Arg {
        const val KEY_MEMBER_ID = "memberId"
        const val KEY_USER_ID = "userId"

        fun SavedStateHandle.getUserId(): UserId = UserId(
            checkNotNull(get<String>(KEY_USER_ID)) { "Missing '$KEY_USER_ID' key in SavedStateHandle" }
        )

        fun SavedStateHandle.getMemberId(): MemberDeviceId = MemberDeviceId(
            checkNotNull(get<String>(KEY_MEMBER_ID)) { "Missing '$KEY_MEMBER_ID' key in SavedStateHandle" }
        )
    }

    public object Route {
        public object AdminApproval {
            public const val Deeplink: String =
                "auth/{${Arg.KEY_USER_ID}}/member/${Arg.KEY_MEMBER_ID}/device/approval"

            public fun get(userId: UserId, memberId: UserId): String =
                "auth/${userId.id}/member/${memberId.id}/device/approval"
        }

        public object SelfApproval {
            public const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/device/approval"
            public fun get(userId: UserId): String = "auth/${userId.id}/device/approval"
        }
    }

    public fun NavGraphBuilder.addSelfApprovalScreen(
        userId: UserId,
        onClose: () -> Unit,
        onError: (String?) -> Unit
    ) {
        composable(
            route = Route.SelfApproval.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
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

    public fun NavGraphBuilder.addAdminApprovalScreen(
        userId: UserId
    ) {
        composable(
            route = Route.AdminApproval.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_MEMBER_ID) { type = NavType.StringType },
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
        ) {
            TODO("Admin approval screen")
        }
    }
}
