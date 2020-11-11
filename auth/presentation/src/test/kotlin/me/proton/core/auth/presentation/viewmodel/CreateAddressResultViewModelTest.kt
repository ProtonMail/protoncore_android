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

import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UpdateExternalAccount
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.auth.presentation.entity.AddressesResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class CreateAddressResultViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val updateExternalAccountUseCase = mockk<UpdateExternalAccount>(relaxed = true)
    private val updateUsernameOnlyUseCase = mockk<UpdateUsernameOnlyAccount>(relaxed = true)
    private val availableDomainsUseCase = mockk<AvailableDomains>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = SessionId("test-sessionId")
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    private val testPassphrase = "test-passphrase"
    // endregion

    private lateinit var viewModel: CreateAddressResultViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { availableDomainsUseCase.invoke() } returns flowOf(
            AvailableDomains.State.Success(listOf("protonmail.com", "protonmail.ch"))
        )
        viewModel = CreateAddressResultViewModel(updateExternalAccountUseCase, updateUsernameOnlyUseCase, availableDomainsUseCase)
    }

    @Test
    fun `upgrade external happy path`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns true
        every { addresses.usernameOnly } returns false

        coEvery {
            updateExternalAccountUseCase.invoke(
                testSessionId,
                testUsername,
                any(),
                testPassphrase.toByteArray()
            )
        } returns
            flowOf(
                UpdateExternalAccount.State.Processing,
                UpdateExternalAccount.State.Success(mockk())
            )

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        val observerUsername = mockk<(UpdateUsernameOnlyAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        viewModel.usernameOnlyAccountUpgradeState.observeDataForever(observerUsername)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        val argumentsUsername = mutableListOf<UpdateUsernameOnlyAccount.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        verify { observerUsername(capture(argumentsUsername)) wasNot called }
        assertIs<UpdateExternalAccount.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UpdateExternalAccount.State.Success)
    }

    @Test
    fun `upgrade external empty username`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns true
        every { addresses.usernameOnly } returns false

        viewModel = CreateAddressResultViewModel(UpdateExternalAccount(mockk(), mockk()), updateUsernameOnlyUseCase, availableDomainsUseCase)

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, "", testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateExternalAccount.State.Error.EmptyCredentials>(state)
    }

    @Test
    fun `upgrade external empty passphrase`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns true
        every { addresses.usernameOnly } returns false

        viewModel = CreateAddressResultViewModel(UpdateExternalAccount(mockk(), mockk()), updateUsernameOnlyUseCase, availableDomainsUseCase)

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, "".toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateExternalAccount.State.Error.EmptyCredentials>(state)
    }

    @Test
    fun `upgrade external empty addresses`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns true
        every { addresses.usernameOnly } returns false

        coEvery {
            updateExternalAccountUseCase.invoke(
                testSessionId,
                testUsername,
                any(),
                testPassphrase.toByteArray()
            )
        } returns
            flowOf(
                UpdateExternalAccount.State.Processing,
                UpdateExternalAccount.State.Success(Addresses(emptyList()))
            )

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<UpdateExternalAccount.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UpdateExternalAccount.State.Success)
        assertNotNull(successState.address)
        assertTrue(successState.address.addresses.isEmpty())
    }

    @Test
    fun `upgrade external set username failed`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns true
        every { addresses.usernameOnly } returns false

        coEvery {
            updateExternalAccountUseCase.invoke(
                testSessionId,
                testUsername,
                any(),
                testPassphrase.toByteArray()
            )
        } returns
            flowOf(
                UpdateExternalAccount.State.Processing,
                UpdateExternalAccount.State.Error.SetUsernameFailed
            )

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<UpdateExternalAccount.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UpdateExternalAccount.State.Error.SetUsernameFailed)
    }

    @Test
    fun `upgrade username-only`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns false

        coEvery {
            updateUsernameOnlyUseCase.invoke(
                testSessionId,
                any(),
                testUsername,
                testPassphrase.toByteArray()
            )
        } returns
            flowOf(
                UpdateUsernameOnlyAccount.State.Processing,
                UpdateUsernameOnlyAccount.State.Success(mockk())
            )

        val observer = mockk<(UpdateUsernameOnlyAccount.State) -> Unit>(relaxed = true)
        val observerExternal = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.usernameOnlyAccountUpgradeState.observeDataForever(observer)
        viewModel.externalAccountUpgradeState.observeDataForever(observerExternal)

        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, testPassphrase.toByteArray())

        // THEN
        val arguments = mutableListOf<UpdateUsernameOnlyAccount.State>()
        val argumentsExternal = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        verify { observerExternal(capture(argumentsExternal)) wasNot called }
        assertIs<UpdateUsernameOnlyAccount.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UpdateUsernameOnlyAccount.State.Success)
    }

    @Test
    fun `upgrade username-only empty username`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns false
        every { addresses.usernameOnly } returns true

        viewModel =
            CreateAddressResultViewModel(updateExternalAccountUseCase, UpdateUsernameOnlyAccount(mockk(), mockk()), availableDomainsUseCase)

        val observer = mockk<(UpdateUsernameOnlyAccount.State) -> Unit>(relaxed = true)
        viewModel.usernameOnlyAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, "", testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateUsernameOnlyAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateUsernameOnlyAccount.State.Error.EmptyCredentials>(state)
    }

    @Test
    fun `upgrade username-only empty passphrase`() = coroutinesTest {
        // GIVEN
        val addresses = mockk<AddressesResult>(relaxed = true)
        every { addresses.allExternal } returns false
        every { addresses.usernameOnly } returns true

        viewModel =
            CreateAddressResultViewModel(updateExternalAccountUseCase, UpdateUsernameOnlyAccount(mockk(), mockk()), availableDomainsUseCase)

        val observer = mockk<(UpdateUsernameOnlyAccount.State) -> Unit>(relaxed = true)
        viewModel.usernameOnlyAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(addresses, testSessionId, testUsername, testDomain, "".toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateUsernameOnlyAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateUsernameOnlyAccount.State.Error.EmptyCredentials>(state)
    }
}
