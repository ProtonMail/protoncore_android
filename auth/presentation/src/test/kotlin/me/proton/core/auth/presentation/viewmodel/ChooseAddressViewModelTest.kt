/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.yield
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.User
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ChooseAddressViewModelTest : ArchTest by ArchTest(), CoroutinesTest by UnconfinedCoroutinesTest() {

    // region mocks
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var usernameDomainAvailability: UsernameDomainAvailability
    // endregion

    private val userId = UserId("userId")
    private val user = mockk<User>()

    private lateinit var viewModel: ChooseAddressViewModel

    @Before
    fun beforeEveryTest() {
        accountWorkflowHandler = mockk(relaxed = true)
        usernameDomainAvailability = mockk(relaxed = true)
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        coEvery { usernameDomainAvailability.getUser(any()) } returns user
        coEvery { user.name } returns null
    }

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        flowTest(viewModel.state) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.State.Processing)

            val data = awaitItem()
            assertTrue(data is ChooseAddressViewModel.State.Data)
            assertEquals(listOf("protonmail.com", "protonmail.ch"), data.domains)
            assertEquals("protonmail.com", data.domains.first())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } coAnswers {
            yield()
            throw ApiException(ApiResult.Error.NoInternet())
        }
        flowTest(viewModel.state) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Processing, "Actual state is $it") }
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Error.Message, "Actual state is $it") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `username availability happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { usernameDomainAvailability.isUsernameAvailable(any(), any()) } returns true
        flowTest(viewModel.state) {
            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.State.Data)
            assertTrue(awaitItem() is ChooseAddressViewModel.State.Processing)

            val successState = awaitItem()
            assertTrue(successState is ChooseAddressViewModel.State.Success)
            assertEquals("test-username", successState.username)
            assertEquals("domain", successState.domain)

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.setUserId(userId)
        viewModel.checkUsername("test-username", "domain")
    }

    @Test
    fun `unavailable username`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { usernameDomainAvailability.isUsernameAvailable(any(), any()) } throws Exception("not available")
        flowTest(viewModel.state) {
            // THEN
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Processing, "Actual state is $it") }
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Data, "Actual state is $it") }
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Processing, "Actual state is $it") }
            awaitItem().let { assertTrue(it is ChooseAddressViewModel.State.Error.Message, "Actual state is $it") }

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.setUserId(userId)
        viewModel.checkUsername("test-username", "domain")
    }
}
