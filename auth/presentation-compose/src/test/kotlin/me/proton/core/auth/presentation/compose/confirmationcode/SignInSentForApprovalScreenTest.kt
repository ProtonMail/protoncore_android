package me.proton.core.auth.presentation.compose.confirmationcode

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import me.proton.core.auth.presentation.compose.sso.device.AvailableDeviceUIModel
import me.proton.core.auth.presentation.compose.sso.device.ClientType
import org.junit.Rule
import org.junit.Test

class SignInSentForApprovalScreenTest {

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
    fun signInSentForApprovalScreenTest() {
        paparazzi.snapshot {
            SignInSentForApprovalScreen(
                onCloseClicked = {},
                onUseBackUpClicked = {},
                onAskAdminClicked = {},
                state = SignInSentForApprovalState.DataLoaded(
                    confirmationCode = "64S3", availableDevices =
                    listOf(
                        AvailableDeviceUIModel(
                            id = "device1",
                            authDeviceName = "Device Name",
                            localizedClientName = "Chrome",
                            lastActivityTime = 242344233124,
                            lastActivityReadable = "7 hours ago",
                            clientType = ClientType.Web
                        )
                    )
                )
            )
        }
    }
}