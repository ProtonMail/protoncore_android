package me.proton.core.devicemigration.presentation.success

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class OriginSuccessScreenTest(deviceConfig: DeviceConfig) {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = deviceConfig)

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

    companion object {
        @Parameters
        @JvmStatic
        fun parameters() = listOf(
            DeviceConfig.PIXEL_5,
            DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT)
        )
    }
}