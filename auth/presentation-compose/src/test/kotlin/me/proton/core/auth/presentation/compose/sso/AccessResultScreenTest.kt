package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import kotlin.test.Test

class AccessResultScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `access granted screen`() {
        paparazzi.snapshot {
            ProtonTheme {
                RequestAccessGrantedScreen()
            }
        }
    }

    @Test
    fun `access denied screen`() {
        paparazzi.snapshot {
            ProtonTheme {
                RequestAccessDeniedScreen()
            }
        }
    }
}