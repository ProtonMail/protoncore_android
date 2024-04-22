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
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun driveStorageUsageIsHigh() {
        paparazzi.snapshot {
            WithSidebarColors {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    title = "Drive: 80% full"
                )
            }
        }
    }

    @Test
    fun driveStorageUsageIsHighDarkTheme() {
        paparazzi.snapshot {
            WithSidebarColors(isDark = true) {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    title = "Drive: 80% full"
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
                    title = "Mail: 100% full"
                )
            }
        }
    }

    @Test
    fun mailStorageUsageIsHighDarkTheme() {
        paparazzi.snapshot {
            WithSidebarColors(isDark = true) {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    title = "Mail: 100% full"
                )
            }
        }
    }

    @Test
    fun mailStorageUsageIsHighWithDividers() {
        val viewModel = mockk<UpgradeStorageInfoViewModel> {
            every { state } returns MutableStateFlow(
                AccountStorageState.HighStorageUsage.Mail(
                    85,
                    UserId("test")
                )
            )
        }

        paparazzi.snapshot {
            WithSidebarColors {
                UpgradeStorageInfo(
                    onUpgradeClicked = {},
                    viewModel = viewModel,
                    withBottomDivider = true,
                    withTopDivider = true
                )
            }
        }
    }
}

@Composable
private fun WithSidebarColors(isDark: Boolean = false, block: @Composable () -> Unit) {
    ProtonTheme(isDark = isDark, colors = ProtonColors.Light.sidebarColors!!) {
        block()
    }
}
