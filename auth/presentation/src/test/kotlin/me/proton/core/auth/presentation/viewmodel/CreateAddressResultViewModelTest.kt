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
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UpdateExternalAccount
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
        viewModel = CreateAddressResultViewModel(updateExternalAccountUseCase, availableDomainsUseCase)
    }

    @Test
    fun `upgrade external empty username`() = coroutinesTest {
        // GIVEN
        viewModel = CreateAddressResultViewModel(UpdateExternalAccount(mockk(), mockk()), availableDomainsUseCase)

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(testSessionId, "", testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateExternalAccount.State.Error.EmptyCredentials>(state)
    }

    @Test
    fun `upgrade external empty passphrase`() = coroutinesTest {
        // GIVEN
        viewModel = CreateAddressResultViewModel(UpdateExternalAccount(mockk(), mockk()), availableDomainsUseCase)

        val observer = mockk<(UpdateExternalAccount.State) -> Unit>(relaxed = true)
        viewModel.externalAccountUpgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(testSessionId, testUsername, testDomain, "".toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val state = arguments[0]
        assertIs<UpdateExternalAccount.State.Error.EmptyCredentials>(state)
    }

    @Test
    fun `upgrade external empty addresses`() = coroutinesTest {
        // GIVEN
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
        viewModel.upgradeAccount(testSessionId, testUsername, testDomain, testPassphrase.toByteArray())
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
        viewModel.upgradeAccount(testSessionId, testUsername, testDomain, testPassphrase.toByteArray())
        // THEN
        val arguments = mutableListOf<UpdateExternalAccount.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<UpdateExternalAccount.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is UpdateExternalAccount.State.Error.SetUsernameFailed)
    }
}
