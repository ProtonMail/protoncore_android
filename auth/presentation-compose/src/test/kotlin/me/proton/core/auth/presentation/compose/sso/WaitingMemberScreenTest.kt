package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import org.junit.Rule
import org.junit.Test

class WaitingMemberScreenTest {

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
            WaitingMemberScreen(
                onCloseClicked = {},
                onBackupPasswordClicked = {},
                onRequestAdminHelpClicked = {},
                state = WaitingMemberState.DataLoaded(
                    confirmationCode = "64S3", availableDevices =
                    listOf(
                        AvailableDeviceUIModel(
                            id = "device1",
                            authDeviceName = "Device Name",
                            localizedClientName = "Chrome",
                            lastActivityTime = 242344233124,
                            lastActivityReadable = "7 hours ago",
                            platform = AuthDevicePlatform.Web
                        )
                    )
                )
            )
        }
    }
}