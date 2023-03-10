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
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.User
import me.proton.core.usersettings.domain.usecase.SetupUsername
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ChooseAddressViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private var accountWorkflowHandler: AccountWorkflowHandler = mockk(relaxed = true)
    private var accountAvailability: AccountAvailability = mockk(relaxed = true)
    private var postLoginAccountSetup: PostLoginAccountSetup = mockk(relaxed = true)
    private var setupUsername: SetupUsername = mockk(relaxed = true)
    // endregion

    private val userId = UserId("userId")
    private val user = mockk<User>()

    private lateinit var viewModel: ChooseAddressViewModel

    @Before
    fun beforeEveryTest() {
        viewModel =
            ChooseAddressViewModel(accountWorkflowHandler, accountAvailability, postLoginAccountSetup, setupUsername)
    }

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Idle)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)

            val data = awaitItem()
            assertTrue(data is ChooseAddressViewModel.ChooseAddressState.Data.Domains)
            assertEquals(listOf("protonmail.com", "protonmail.ch"), data.domains)
            assertEquals("protonmail.com", data.domains.first())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getDomains() } throws ApiException(ApiResult.Error.NoInternet())

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Idle)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Error.Message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `username available`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getUser(any()) } returns user
        coEvery { user.name } returns null
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { accountAvailability.checkUsername(any<UserId>(), any()) } returns Unit

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Idle)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Data.Domains)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            val state = awaitItem()
            assertTrue(state is ChooseAddressViewModel.ChooseAddressState.Data.UsernameAvailable)
            assertEquals("testemail", state.username)
            assertEquals("protonmail.com", state.domain)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `username unavailable`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getUser(any()) } returns user
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { accountAvailability.checkUsername(any<String>(), any()) } coAnswers {
            yield()
            throw ApiException(
                ApiResult.Error.Http(
                    httpCode = 123,
                    "http error",
                    ApiResult.Error.ProtonData(
                        code = 12106,
                        error = "username not available"
                    )
                )
            )
        }

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Idle)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Data.Domains)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Error.UsernameNotAvailable)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `username unavailable then available`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getUser(any()) } returns user
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
        coEvery { accountAvailability.checkUsername(any()) } coAnswers {
            yield()
            throw ApiException(
                ApiResult.Error.Http(
                    httpCode = 123,
                    "http error",
                    ApiResult.Error.ProtonData(
                        code = 12106,
                        error = "username not available"
                    )
                )
            )
        }

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Idle)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Data.Domains)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Error.UsernameNotAvailable)

            coEvery { accountAvailability.checkUsername(any()) } returns Unit
            viewModel.checkUsername("new-username", "new-domain")
            assertTrue(awaitItem() is ChooseAddressViewModel.ChooseAddressState.Processing)

            val usernameState = awaitItem()
            assertTrue(usernameState is ChooseAddressViewModel.ChooseAddressState.Data.UsernameAvailable)
            assertEquals("new-username", usernameState.username)
            assertEquals("new-domain", usernameState.domain)

            cancelAndConsumeRemainingEvents()
        }
    }
}
