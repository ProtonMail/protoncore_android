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

package me.proton.core.accountrecovery.presentation.compose.ui

import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import me.proton.core.accountrecovery.presentation.compose.dialog.AccountRecoveryDialog
import me.proton.core.domain.entity.UserId

internal object Arg {
    const val UserId = "userId"
}

internal object Route {
    object Recovery {
        const val Deeplink = "users/{${Arg.UserId}}/recovery"
        fun get(userId: UserId) = "users/${userId.id}/recovery"
    }
}

internal fun NavGraphBuilder.addAccountRecoveryDialog(
    userId: UserId,
    onClosed: (Boolean) -> Unit,
    onError: (Throwable?) -> Unit
) {
    dialog(
        route = Route.Recovery.Deeplink,
        arguments = listOf(
            navArgument(Arg.UserId) {
                type = NavType.StringType
                defaultValue = userId.id
            },
        ),
        dialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.SecureOn
        )
    ) {
        AccountRecoveryDialog(
            onClosed = { onClosed(it) },
            onError = { onError(it) }
        )
    }
}
