package me.proton.core.devicemigration.presentation.intro

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.BeforeTest

class SignInIntroScreenTest {
    @MockK(relaxed = true)
    private lateinit var activity: Activity

    @MockK(relaxed = true)
    private lateinit var activityResultRegistryOwner: ActivityResultRegistryOwner

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `idle state`() {
        paparazzi.snapshot {
            UiTestCase {
                SignInIntroScreen(
                    state = SignInIntroState.Idle,
                    effect = null
                )
            }
        }
    }

    @Test
    fun `verifying state`() {
        paparazzi.snapshot {
            UiTestCase {
                SignInIntroScreen(
                    state = SignInIntroState.Verifying,
                    effect = null
                )
            }
        }
    }

    @Composable
    private fun UiTestCase(content: @Composable () -> Unit) {
        ProtonTheme {
            CompositionLocalProvider(
                LocalActivity provides activity,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner
            ) {
                content()
            }
        }
    }
}
