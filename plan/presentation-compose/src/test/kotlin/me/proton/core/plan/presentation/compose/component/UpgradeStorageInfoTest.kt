package me.proton.core.plan.presentation.compose.component

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.compose.viewmodel.AccountStorageState
import me.proton.core.plan.presentation.compose.viewmodel.UpgradeStorageInfoViewModel
import org.junit.Rule
import kotlin.test.Test

class UpgradeStorageInfoTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun viewIsHidden() {
        val viewModel = mockk<UpgradeStorageInfoViewModel> {
            every { state } returns MutableStateFlow(AccountStorageState.Hidden)
        }
        paparazzi.snapshot {
            WithSidebarColors {
                UpgradeStorageInfo(viewModel = viewModel)
            }
        }
    }

    @Test
    fun driveStorageUsageIsHigh() {
        paparazzi.snapshot {
            WithSidebarColors {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    title = "Drive storage: 80% full"
                )
            }
        }
    }

    @Test
    fun mailStorageUsageIsHigh() {
        paparazzi.snapshot {
            WithSidebarColors {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    title = "Mail storage: 100% full"
                )
            }
        }
    }
}

@Composable
private fun WithSidebarColors(block: @Composable () -> Unit) {
    ProtonTheme(colors = ProtonColors.Light.sidebarColors!!) {
        block()
    }
}
