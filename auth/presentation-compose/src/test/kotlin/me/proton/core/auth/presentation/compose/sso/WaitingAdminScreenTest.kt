package me.proton.core.auth.presentation.compose.sso

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class WaitingAdminScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
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
                    adminEmail = "admin@privacybydefault.com",
                    username = "test-username",
                    canUseBackupPassword = true
                )
            )
        }
    }
}
