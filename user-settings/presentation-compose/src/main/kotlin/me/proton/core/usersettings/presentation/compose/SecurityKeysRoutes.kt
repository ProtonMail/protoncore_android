package me.proton.core.usersettings.presentation.compose

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.presentation.compose.view.SecurityKeysScreen

object SecurityKeysRoutes {

    internal object Arg {
        const val KEY_USER_ID = "userId"

        fun SavedStateHandle.getUserId(): UserId = UserId(
            checkNotNull(get<String>(KEY_USER_ID)) { "Missing '$KEY_USER_ID' key in SavedStateHandle" }
        )
    }

    object Route {
        object SecurityKeys {
            const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/settings/keys"
            fun get(userId: UserId): String = "auth/${userId.id}/settings/keys"
        }
    }

    fun NavGraphBuilder.addSecurityKeysScreen(
        userId: UserId,
        onClose: () -> Unit,
        onAddSecurityKeyClicked: () -> Unit,
        onManageSecurityKeysClicked: () -> Unit,
    ) {
        composable(
            route = Route.SecurityKeys.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            ),
        ) {
            SecurityKeysScreen(
                onAddSecurityKeyClicked = onAddSecurityKeyClicked,
                onManageSecurityKeysClicked = onManageSecurityKeysClicked,
                onBackClick = onClose
            )
        }
    }
}