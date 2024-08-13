package me.proton.core.usersettings.presentation.compose.view

import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test

class SecurityKeysScreenKtTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun securityKeysScreen() {
        paparazzi.snapshot {
            ProtonTheme {
                SecurityKeysScreen(
                    onManageSecurityKeysClicked = {},
                    onAddSecurityKeyClicked = {},
                    onBackClick = {}
                )
            }
        }
    }

}