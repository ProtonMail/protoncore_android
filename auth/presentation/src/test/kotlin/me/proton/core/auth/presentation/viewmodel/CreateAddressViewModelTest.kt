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
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UsernameAvailability
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class CreateAddressViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val usernameAvailabilityUseCase = mockk<UsernameAvailability>(relaxed = true)
    private val availableDomainsUseCase = mockk<AvailableDomains>(relaxed = true)
    // endregion

    private lateinit var viewModel: CreateAddressViewModel

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { availableDomainsUseCase.invoke() } returns flowOf(
            AvailableDomains.State.Success(listOf("protonmail.com", "protonmail.ch"))
        )
        viewModel = CreateAddressViewModel(usernameAvailabilityUseCase, availableDomainsUseCase)
        val observer = mockk<(AvailableDomains.State) -> Unit>(relaxed = true)
        viewModel.domainsState.observeDataForever(observer)
        // THEN
        val arguments = mutableListOf<AvailableDomains.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val successState = arguments[0]
        assertTrue(successState is AvailableDomains.State.Success)
        assertEquals(listOf("protonmail.com", "protonmail.ch"), successState.availableDomains)
        assertEquals("protonmail.com", successState.firstOrDefault)
    }

    @Test
    fun `empty available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { availableDomainsUseCase.invoke() } returns flowOf(
            AvailableDomains.State.Error.NoAvailableDomains
        )
        viewModel = CreateAddressViewModel(usernameAvailabilityUseCase, availableDomainsUseCase)
        val observer = mockk<(AvailableDomains.State) -> Unit>(relaxed = true)
        viewModel.domainsState.observeDataForever(observer)
        // THEN
        val arguments = mutableListOf<AvailableDomains.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertTrue(state is AvailableDomains.State.Error.NoAvailableDomains)
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { availableDomainsUseCase.invoke() } returns flowOf(
            AvailableDomains.State.Error.Message("API error")
        )
        viewModel = CreateAddressViewModel(usernameAvailabilityUseCase, availableDomainsUseCase)
        val observer = mockk<(AvailableDomains.State) -> Unit>(relaxed = true)
        viewModel.domainsState.observeDataForever(observer)
        // THEN
        val arguments = mutableListOf<AvailableDomains.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertTrue(state is AvailableDomains.State.Error.Message)
        assertEquals("API error", state.message)
    }

    @Test
    fun `username availability happy path`() = coroutinesTest {
        // GIVEN
        coEvery { usernameAvailabilityUseCase.invoke("test-username") } returns flowOf(
            UsernameAvailability.State.Processing,
            UsernameAvailability.State.Success(true, "test-username")
        )
        viewModel = CreateAddressViewModel(usernameAvailabilityUseCase, availableDomainsUseCase)
        val observer = mockk<(UsernameAvailability.State) -> Unit>(relaxed = true)
        viewModel.usernameState.observeDataForever(observer)
        // WHEN
        viewModel.checkUsernameAvailability("test-username")
        // THEN
        val arguments = mutableListOf<UsernameAvailability.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<UsernameAvailability.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UsernameAvailability.State.Success)
        assertTrue(successState.available)
        assertEquals("test-username", successState.username)
    }

    @Test
    fun `unavailable username`() = coroutinesTest {
        // GIVEN
        coEvery { usernameAvailabilityUseCase.invoke("test-username") } returns flowOf(
            UsernameAvailability.State.Processing,
            UsernameAvailability.State.Error.Message("username is unavailable")
        )
        viewModel = CreateAddressViewModel(usernameAvailabilityUseCase, availableDomainsUseCase)
        val observer = mockk<(UsernameAvailability.State) -> Unit>(relaxed = true)
        viewModel.usernameState.observeDataForever(observer)
        // WHEN
        viewModel.checkUsernameAvailability("test-username")
        // THEN
        val arguments = mutableListOf<UsernameAvailability.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<UsernameAvailability.State.Processing>(arguments[0])
        val state = arguments[1]
        assertTrue(state is UsernameAvailability.State.Error.Message)
        assertEquals("username is unavailable", state.message)
    }

    @Test
    fun `empty username`() = coroutinesTest {
        // GIVEN
        viewModel = CreateAddressViewModel(UsernameAvailability(mockk()), availableDomainsUseCase)
        val observer = mockk<(UsernameAvailability.State) -> Unit>(relaxed = true)
        viewModel.usernameState.observeDataForever(observer)
        // WHEN
        viewModel.checkUsernameAvailability("")
        // THEN
        val arguments = mutableListOf<UsernameAvailability.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        assertIs<UsernameAvailability.State.Error.EmptyUsername>(arguments[0])
    }
}
