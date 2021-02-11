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
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupUsername
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CreateAddressViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val setupUsername = mockk<SetupUsername>(relaxed = true)
    private val setupOriginalAddress = mockk<SetupOriginalAddress>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    // endregion

    private lateinit var viewModel: CreateAddressViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = CreateAddressViewModel(
            accountHandler,
            setupUsername,
            setupOriginalAddress
        )
        coEvery { sessionProvider.getUserId(any()) } returns testUserId
    }

    @Test
    fun `setup username and address`() = coroutinesTest {
        // GIVEN
        coEvery { setupUsername.invoke(testUserId, testUsername) } returns Unit
        coEvery { setupOriginalAddress.invoke(testUserId, testUsername) } returns Unit

        val observer = mockk<(CreateAddressViewModel.State) -> Unit>(relaxed = true)
        viewModel.upgradeState.observeDataForever(observer)
        // WHEN
        viewModel.upgradeAccount(testUserId, testUsername, testDomain)
        // THEN
        val arguments = mutableListOf<CreateAddressViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<CreateAddressViewModel.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is CreateAddressViewModel.State.Success)
    }
}
