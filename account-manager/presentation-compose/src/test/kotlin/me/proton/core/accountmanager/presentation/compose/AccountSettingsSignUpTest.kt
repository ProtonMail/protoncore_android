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

import app.cash.paparazzi.Paparazzi
import io.mockk.every
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
            ProtonTheme {
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
            ProtonTheme(isDark = true) {
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
            ProtonTheme {
                AccountSettingsLoggedIn(
                    onAccountClicked = {},
                    onSignOutClicked = {},
                    state = AccountSettingsViewState.LoggedIn(
                        userId = UserId("test-user-id"),
                        initials = "SN",
                        displayName = "Display Name",
                        email = "email@proton.com"
                    )
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsInfoDark() {
        paparazzi.snapshot {
            ProtonTheme(isDark = true) {
                AccountSettingsLoggedIn(
                    onAccountClicked = {},
                    onSignOutClicked = {},
                    state = AccountSettingsViewState.LoggedIn(
                        userId = UserId("test-user-id"),
                        initials = "SN",
                        displayName = "Display Name",
                        email = "email@proton.com"
                    )
                )
            }
        }
    }

    @Test
    fun defaultAccountSettingsInfoHidden() {
        val viewModel = mockk<AccountSettingsViewModel> {
            every { state } returns MutableStateFlow(AccountSettingsViewState.Hidden)
            every { initialState } returns AccountSettingsViewState.Hidden
        }
        paparazzi.snapshot {
            ProtonTheme(isDark = true) {
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
