package me.proton.core.devicemigration.presentation.success

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test

class OriginSuccessScreenTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `loading state`() {
        paparazzi.snapshot {
            ProtonTheme {
                OriginSuccessScreen(state = OriginSuccessState.Loading)
            }
        }
    }

    @Test
    fun `idle state`() {
        paparazzi.snapshot {
            ProtonTheme {
                OriginSuccessScreen(state = OriginSuccessState.Idle("test@example.test"))
            }
        }
    }
}