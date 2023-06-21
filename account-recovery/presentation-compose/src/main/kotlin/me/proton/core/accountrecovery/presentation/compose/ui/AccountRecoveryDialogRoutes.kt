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
    onClosed: () -> Unit,
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
            onClosed = { onClosed() },
            onError = { onError(it) }
        )
    }
}
