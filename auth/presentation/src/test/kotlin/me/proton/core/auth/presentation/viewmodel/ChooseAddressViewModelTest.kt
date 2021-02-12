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
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.firstOrDefault
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ChooseAddressViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountWorkflowHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val usernameDomainAvailability = mockk<UsernameDomainAvailability>(relaxed = true)
    // endregion

    private val userId = UserId("userId")
    private val user = mockk<User>()

    private lateinit var viewModel: ChooseAddressViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        coEvery { usernameDomainAvailability.getUser(any()) } returns user
        coEvery { user.name } returns null
    }

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.State) -> Unit>(relaxed = true)
        viewModel.state.observeDataForever(observer)
        viewModel.setUserId(userId)
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertTrue(arguments[0] is ChooseAddressViewModel.State.Processing)
        val dataState = arguments[1]
        assertTrue(dataState is ChooseAddressViewModel.State.Data)
        assertEquals(listOf("protonmail.com", "protonmail.ch"), dataState.domains)
        assertEquals("protonmail.com", dataState.domains.firstOrDefault())
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } throws ApiException(
            ApiResult.Error.NoInternet
        )
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.State) -> Unit>(relaxed = true)
        viewModel.state.observeDataForever(observer)
        viewModel.setUserId(userId)
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertTrue(arguments[0] is ChooseAddressViewModel.State.Processing)
        assertTrue(arguments[1] is ChooseAddressViewModel.State.Error.Message)
    }

    @Test
    fun `username availability happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { usernameDomainAvailability.isUsernameAvailable(any(), any()) } returns true
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.State) -> Unit>(relaxed = true)
        viewModel.state.observeDataForever(observer)
        viewModel.setUserId(userId)
        viewModel.checkUsername("test-username", "domain")
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        assertTrue(arguments[0] is ChooseAddressViewModel.State.Processing)
        assertTrue(arguments[1] is ChooseAddressViewModel.State.Data)
        assertTrue(arguments[2] is ChooseAddressViewModel.State.Processing)
        val successState = arguments[3]
        assertTrue(successState is ChooseAddressViewModel.State.Success)
        assertEquals("test-username", successState.username)
        assertEquals("domain", successState.domain)
    }

    @Test
    fun `unavailable username`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { usernameDomainAvailability.isUsernameAvailable(any(), any()) } returns false
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.State) -> Unit>(relaxed = true)
        viewModel.state.observeDataForever(observer)
        viewModel.setUserId(userId)
        viewModel.checkUsername("test-username", "domain")
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        assertIs<ChooseAddressViewModel.State.Processing>(arguments[0])
        assertIs<ChooseAddressViewModel.State.Data>(arguments[1])
        assertIs<ChooseAddressViewModel.State.Processing>(arguments[2])
        val state = arguments[3]
        assertTrue(state is ChooseAddressViewModel.State.Error.UsernameNotAvailable)
    }
}
