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
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.entity.firstOrDefault
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ChooseAddressViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountWorkflowHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val usernameDomainAvailability = mockk<UsernameDomainAvailability>(relaxed = true)
    // endregion

    private lateinit var viewModel: ChooseAddressViewModel

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.DomainState) -> Unit>(relaxed = true)
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        viewModel.domainsState.observeDataForever(observer)
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.DomainState>()
        verify { observer(capture(arguments)) }
        val successState = arguments[0]
        assertTrue(successState is ChooseAddressViewModel.DomainState.Success)
        assertEquals(listOf("protonmail.com", "protonmail.ch"), successState.domains)
        assertEquals("protonmail.com", successState.domains.firstOrDefault())
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.getDomains() } throws ApiException(
            ApiResult.Error.NoInternet
        )
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.DomainState) -> Unit>(relaxed = true)
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        viewModel.domainsState.observeDataForever(observer)
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.DomainState>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertTrue(state is ChooseAddressViewModel.DomainState.Error.Message)
    }

    @Test
    fun `username availability happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.isUsernameAvailable(any()) } returns true
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.UsernameState) -> Unit>(relaxed = true)
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        viewModel.usernameState.observeDataForever(observer)
        viewModel.checkUsernameAvailability("test-username")
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.UsernameState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<ChooseAddressViewModel.UsernameState.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is ChooseAddressViewModel.UsernameState.Success)
        assertTrue(successState.available)
        assertEquals("test-username", successState.username)
    }

    @Test
    fun `unavailable username`() = coroutinesTest {
        // GIVEN
        coEvery { usernameDomainAvailability.isUsernameAvailable(any()) } returns false
        // WHEN
        val observer = mockk<(ChooseAddressViewModel.UsernameState) -> Unit>(relaxed = true)
        viewModel = ChooseAddressViewModel(accountWorkflowHandler, usernameDomainAvailability)
        viewModel.usernameState.observeDataForever(observer)
        viewModel.checkUsernameAvailability("test-username")
        // THEN
        val arguments = mutableListOf<ChooseAddressViewModel.UsernameState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<ChooseAddressViewModel.UsernameState.Processing>(arguments[0])
        val state = arguments[1]
        assertTrue(state is ChooseAddressViewModel.UsernameState.Success)
        assertFalse(state.available)
    }
}
