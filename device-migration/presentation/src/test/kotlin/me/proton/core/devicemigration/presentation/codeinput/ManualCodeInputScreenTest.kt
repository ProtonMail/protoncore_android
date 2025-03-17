package me.proton.core.devicemigration.presentation.codeinput

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test

class ManualCodeInputScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `idle state`() {
        paparazzi.snapshot {
            ProtonTheme {
                ManualCodeInputScreen(
                    state = ManualCodeInputState.Idle,
                    effect = null
                )
            }
        }
    }

    @Test
    fun `empty code state`() {
        paparazzi.snapshot {
            ProtonTheme {
                ManualCodeInputScreen(
                    state = ManualCodeInputState.Error.EmptyCode,
                    effect = null
                )
            }
        }
    }
}
