package me.proton.core.usersettings.presentation.compose.view

import app.cash.paparazzi.Paparazzi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.usersettings.presentation.compose.viewmodel.SecurityKeysInfoViewModel
import me.proton.core.usersettings.presentation.compose.viewmodel.SecurityKeysState
import org.junit.Rule
import org.junit.Test

class SecurityKeysListKtTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun securityKeysList() {
        val viewModel = mockk<SecurityKeysInfoViewModel> {
            every { state } returns MutableStateFlow(
                SecurityKeysState.Success(
                    listOf(
                        Fido2RegisteredKey("format", UByteArray(10), "Test key 1"),
                        Fido2RegisteredKey("format", UByteArray(10), "Test key 2"),
                    )
                )
            )
        }
        paparazzi.snapshot {
            ProtonTheme {
                SecurityKeysList(viewModel = viewModel)
            }
        }
    }

    @Test
    fun securityKeysLoading() {
        val viewModel = mockk<SecurityKeysInfoViewModel> {
            every { state } returns MutableStateFlow(
                SecurityKeysState.Loading
            )
        }
        paparazzi.snapshot {
            ProtonTheme {
                SecurityKeysList(viewModel = viewModel)
            }
        }
    }

    @Test
    fun securityKeysError() {
        val viewModel = mockk<SecurityKeysInfoViewModel> {
            every { state } returns MutableStateFlow(
                SecurityKeysState.Error(Error("Test error."))
            )
        }
        paparazzi.snapshot {
            ProtonTheme {
                SecurityKeysList(viewModel = viewModel)
            }
        }
    }
}