/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.accountmanager.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.presentation.entity.AccountItem
import me.proton.core.accountmanager.presentation.view.AccountPrimaryView
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test

class SnapshotAccountPrimaryViewTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun `accountPrimaryView filled with data`() = runTest {
        val userId = UserId("test-user")
        val initials = "TA"
        val name = "Test Account"
        val email = "test.account@example.com"
        val accountSwitcherViewModel = mockk<AccountSwitcherViewModel>()
        val primaryAccountSharedFlow = MutableSharedFlow<AccountItem?>()
        every { accountSwitcherViewModel.primaryAccount } returns primaryAccountSharedFlow

        primaryAccountSharedFlow.emit(
            AccountItem(userId, "TU", "test-user", "test-user@example.com", AccountState.Ready)
        )

        val view = AccountPrimaryView(paparazzi.context).also { view ->
            view.setViewModel(accountSwitcherViewModel)
            view.initials = initials
            view.name = name
            view.email = email
        }
        paparazzi.snapshot(view, "default")
    }

    @Test
    fun `empty accountPrimaryView`() = runTest {
        val userId = UserId("test-user")
        val accountSwitcherViewModel = mockk<AccountSwitcherViewModel>()
        val primaryAccountSharedFlow = MutableSharedFlow<AccountItem?>()
        every { accountSwitcherViewModel.primaryAccount } returns primaryAccountSharedFlow

        primaryAccountSharedFlow.emit(
            AccountItem(userId, "TU", "test-user", "test-user@example.com", AccountState.Ready)
        )

        val view = AccountPrimaryView(paparazzi.context).also { view ->
            view.setViewModel(accountSwitcherViewModel)
        }
        paparazzi.snapshot(view, "default")
    }
}