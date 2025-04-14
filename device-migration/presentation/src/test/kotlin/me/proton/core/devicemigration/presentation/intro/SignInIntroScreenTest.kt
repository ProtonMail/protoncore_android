package me.proton.core.devicemigration.presentation.intro

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.Product
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.BeforeTest

@RunWith(Parameterized::class)
class SignInIntroScreenTest(deviceConfig: DeviceConfig) {
    @MockK(relaxed = true)
    private lateinit var activity: Activity

    @MockK(relaxed = true)
    private lateinit var activityResultRegistryOwner: ActivityResultRegistryOwner

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = deviceConfig)

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
    fun `missing permission state`() {
        paparazzi.snapshot {
            UiTestCase {
                SignInIntroScreen(
                    state = SignInIntroState.MissingCameraPermission(Product.Mail),
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
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalInspectionMode provides true
            ) {
                content()
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
