package me.proton.core.auth.presentation.compose.confirmationcode

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import org.junit.Rule
import org.junit.Test

class SignInRequestedForApprovalScreenTest {
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
    fun signInRequestedForApprovalScreenTest() {
        paparazzi.snapshot {
            SignInRequestedForApprovalScreen(
                onCloseClicked = {},
                onConfirmClicked = {},
                onRejectClicked = {},
                state = SignInRequestedForApprovalState.Idle(email = "user@example.test")
            )
        }
    }
}