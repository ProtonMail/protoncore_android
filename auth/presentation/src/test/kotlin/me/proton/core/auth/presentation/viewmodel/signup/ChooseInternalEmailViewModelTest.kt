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

package me.proton.core.auth.presentation.viewmodel.signup

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.presentation.viewmodel.signup.ChooseInternalEmailViewModel.State
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChooseInternalEmailViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    private val accountAvailability = mockk<AccountAvailability>(relaxed = true)

    private lateinit var viewModel: ChooseInternalEmailViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { accountAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
    }

    @Test
    fun `domains are loaded correctly`() = coroutinesTest {
        // GIVEN
        viewModel = ChooseInternalEmailViewModel(accountAvailability)
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is State.Idle)
            assertTrue(awaitItem() is State.Processing)
            val domainsItem = awaitItem() as State.Domains
            assertEquals(listOf("protonmail.com", "protonmail.ch"), domainsItem.domains)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `domains loading connectivity error`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getDomains() } throws ApiException(
            ApiResult.Error.NoInternet()
        )
        viewModel = ChooseInternalEmailViewModel(accountAvailability)
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is State.Idle)
            assertTrue(awaitItem() is State.Processing)
            assertTrue(awaitItem() is State.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `domains loading api error`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getDomains() } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "domains error"
                )
            )
        )
        viewModel = ChooseInternalEmailViewModel(accountAvailability)
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is State.Idle)
            assertTrue(awaitItem() is State.Processing)
            val errorItem = awaitItem() as State.Error.Message
            assertEquals("domains error", errorItem.error.getUserMessage(mockk()))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { accountAvailability.checkUsername(testEmail) } returns Unit
        viewModel = ChooseInternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkUsername(testUsername, testDomain)
            // THEN
            assertTrue(awaitItem() is State.Idle)
            assertTrue(awaitItem() is State.Processing)
            assertTrue(awaitItem() is State.Domains)
            assertTrue(awaitItem() is State.Processing)
            val item = awaitItem() as State.Success
            assertEquals(testUsername, item.username)
            assertEquals(testDomain, item.domain)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username error`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { accountAvailability.checkUsername(testEmail) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "username not available"
                )
            )
        )
        // WHEN
        viewModel = ChooseInternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkUsername(testUsername, testDomain)
            // THEN
            assertTrue(awaitItem() is State.Idle)
            assertTrue(awaitItem() is State.Processing)
            assertTrue(awaitItem() is State.Domains)
            assertTrue(awaitItem() is State.Processing)
            assertTrue(awaitItem() is State.Error.Message)
            cancelAndConsumeRemainingEvents()
        }
    }
}
