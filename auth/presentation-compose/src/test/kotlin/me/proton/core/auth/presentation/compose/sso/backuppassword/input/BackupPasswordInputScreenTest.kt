package me.proton.core.auth.presentation.compose.sso.backuppassword.input

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.utils.StringBox
import org.junit.Rule
import kotlin.test.Test

class BackupPasswordInputScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `initial screen`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordInputScreen(state = BackupPasswordInputState.Idle)
            }
        }
    }

    @Test
    fun `loading screen`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordInputScreen(state = BackupPasswordInputState.Loading)
            }
        }
    }

    @Test
    fun `invalid password`() {
        paparazzi.snapshot {
            ProtonTheme {
                BackupPasswordInputScreen(
                    state = BackupPasswordInputState.FormError(R.string.backup_password_input_password_empty)
                )
            }
        }
    }
}
