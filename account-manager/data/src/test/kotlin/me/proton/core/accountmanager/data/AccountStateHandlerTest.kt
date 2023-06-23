/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountmanager.data

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.migrator.AccountMigrator
import me.proton.core.domain.entity.Product
import me.proton.core.notification.presentation.NotificationSetup
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.user.domain.UserManager
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class AccountStateHandlerTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var accountMigrator: AccountMigrator

    @MockK
    private lateinit var notificationSetup: NotificationSetup

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: AccountStateHandler

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun noAccounts() {
        // GIVEN
        makeTested()
        every { accountManager.onAccountStateChanged(true) } returns flowOf()
        every { accountManager.getAccounts() } returns flowOf()
        justRun { notificationSetup.invoke() }

        // WHEN
        tested.start()

        // THEN
        coVerify { notificationSetup() }
    }

    private fun makeTested(product: Product = Product.Mail) {
        tested = AccountStateHandler(
            scopeProvider = TestCoroutineScopeProvider(dispatchers),
            userManager = userManager,
            accountManager = accountManager,
            accountRepository = accountRepository,
            accountMigrator = accountMigrator,
            notificationSetup = notificationSetup,
            product = product
        ).let { spyk(it) }
    }
}