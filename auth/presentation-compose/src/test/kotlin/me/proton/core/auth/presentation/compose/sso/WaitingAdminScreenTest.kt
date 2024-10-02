package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import org.junit.Rule
import org.junit.Test

class WaitingAdminScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme",
        // Remove when layoutlib properly supports SDK 34 (https://github.com/cashapp/paparazzi/issues/1025).
        environment = detectEnvironment().run {
            copy(compileSdkVersion = 33, platformDir = platformDir.replace("34", "33"))
        }
    )

    @Test
    fun shareConfirmationCodeWithAdminScreenTest() {
        paparazzi.snapshot {
            WaitingAdminScreen(
                onCloseClicked = {},
                onBackupPasswordClicked = {},
                onErrorMessage = {},
                state = WaitingAdminState.DataLoaded(
                    confirmationCode = "64S3",
                    username = "test-username",
                )
            )
        }
    }
}
