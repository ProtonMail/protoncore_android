/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.crypto.validator.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.validator.domain.prefs.CryptoPrefs
import me.proton.core.domain.entity.UserId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

internal class CryptoValidatorErrorViewModelTest : ArchTest by ArchTest(),
    CoroutinesTest by UnconfinedCoroutinesTest() {

    lateinit var viewModel: CryptoValidatorErrorViewModel
    private val accountManager = mockk<AccountManager> {
        val account = Account(
            UserId("some_id"),
            "someUser",
            "a@b.com",
            AccountState.Removed,
            null,
            null,
            AccountDetails(null, null)
        )
        every { getAccounts() } returns flowOf(listOf(account))
    }
    private val cryptoPrefs = mockk<CryptoPrefs>(relaxed = true)

    @Before
    fun setup() {
        viewModel = CryptoValidatorErrorViewModel(accountManager, cryptoPrefs)
    }

    @Test
    fun `hasAccounts returns a flow with the current accounts`() = runTest {
        // WHEN
        val hasAccounts = viewModel.hasAccounts
        // THEN
        verify { accountManager.getAccounts() }
        hasAccounts.test { assertTrue(awaitItem()) }
    }

    @Test
    fun `hasAccounts returns false if there are no accounts`() = runTest {
        // GIVEN
        every { accountManager.getAccounts() } returns flowOf(emptyList())
        // WHEN
        val hasAccountsFlow = viewModel.hasAccounts
        // THEN
        verify { accountManager.getAccounts() }
        hasAccountsFlow.test { assertTrue(awaitItem()) }
    }

    @Test
    fun `allowInsecureKeystore saves the value in cryptoPrefs`() {
        // GIVEN
        var savedValue = false
        every { cryptoPrefs.useInsecureKeystore = any() } propertyType Boolean::class answers { savedValue = value }
        every { cryptoPrefs.getProperty("useInsecureKeystore") } returns { savedValue }
        // WHEN
        viewModel.allowInsecureKeystore()
        // THEN
        verify { cryptoPrefs.useInsecureKeystore = true }
        assertTrue(savedValue)
    }

    @Test
    fun `removeAllAccounts removes all accounts`() = runTest {
        // GIVEN
        coEvery { accountManager.removeAccount(any()) } returns Unit
        // WHEN
        viewModel.removeAllAccounts()
        // THEN
        verify { accountManager.getAccounts() }
        val accounts = withTimeout(1000) { accountManager.getAccounts().first() }
        coVerify(exactly = accounts.count()) { accountManager.removeAccount(any()) }
    }
}
