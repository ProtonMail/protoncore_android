/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.accountmanager.presentation.compose

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test

class AccountSettingsSignUpTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun defaultAccountSettingsViewLight() {
        paparazzi.snapshot {
            WithProtonTheme {
                AccountSettingsCredentialLess(
                    onCreateAccountClicked = {},
                    onSignInClicked = {}
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsViewDark() {
        paparazzi.snapshot {
            WithProtonTheme(isDark = true) {
                AccountSettingsCredentialLess(
                    onCreateAccountClicked = {},
                    onSignInClicked = {}
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsInfoLight() {
        paparazzi.snapshot {
            WithProtonTheme {
                AccountSettingsLoggedIn(
                    onAccountClicked = {},
                    onSignOutClicked = {},
                    loggedIn = AccountSettingsViewState.LoggedIn(
                        UserId("test-user-id"),
                        "SN",
                        "Display Name",
                        "email@proton.com"
                    )
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsInfoDark() {
        paparazzi.snapshot {
            WithProtonTheme(isDark = true) {
                AccountSettingsLoggedIn(
                    onAccountClicked = {},
                    onSignOutClicked = {},
                    loggedIn = AccountSettingsViewState.LoggedIn(
                        UserId("test-user-id"),
                        "SN",
                        "Display Name",
                        "email@proton.com"
                    )
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsInfoHidden() {
        val viewModel = mockk<AccountSettingsViewModel> {
            io.mockk.every { state } returns MutableStateFlow(
                AccountSettingsViewState.Hidden
            )
        }
        paparazzi.snapshot {
            WithProtonTheme(isDark = true) {
                AccountSettingsInfo(
                    onAccountClicked = {},
                    onSignOutClicked = {},
                    onSignUpClicked = {},
                    onSignInClicked = {},
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun WithProtonTheme(isDark: Boolean = false, block: @Composable () -> Unit) {
    ProtonTheme(isDark = isDark) {
        block()
    }
}
