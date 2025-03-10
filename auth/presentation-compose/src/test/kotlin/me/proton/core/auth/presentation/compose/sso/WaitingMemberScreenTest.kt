package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import org.junit.Rule
import org.junit.Test

class WaitingMemberScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
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
                        AuthDeviceData(
                            deviceId = AuthDeviceId("device1"),
                            name = "Device Name",
                            localizedClientName = "Chrome",
                            lastActivityTime = 242344233124,
                            lastActivityReadable = "Last used 7 hours ago",
                            platform = AuthDevicePlatform.Web
                        )
                    )
                )
            )
        }
    }
}