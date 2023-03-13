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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.yield
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.User
import me.proton.core.usersettings.domain.usecase.SetupUsername
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class ChooseAddressViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    @MockK(relaxed = true)
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler

    @MockK(relaxed = true)
    private lateinit var accountAvailability: AccountAvailability

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK(relaxed = true)
    private lateinit var postLoginAccountSetup: PostLoginAccountSetup

    @MockK(relaxed = true)
    private lateinit var setupUsername: SetupUsername
    // endregion

    private val userId = UserId("userId")

    @MockK
    private lateinit var user: User

    private lateinit var viewModel: ChooseAddressViewModel

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        viewModel =
            ChooseAddressViewModel(
                accountWorkflowHandler,
                accountAvailability,
                observabilityManager,
                postLoginAccountSetup,
                setupUsername
            )
    }

    @Test
    fun `available domains happy path`() = coroutinesTest {
        // GIVEN
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains(any()) } returns listOf(
            "protonmail.com",
            "protonmail.ch"
        )

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())

            val data = awaitItem()
            assertIs<ChooseAddressViewModel.ChooseAddressState.Data.Domains>(data)
            assertEquals(listOf("protonmail.com", "protonmail.ch"), data.domains)
            assertEquals("protonmail.com", data.domains.first())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `available domains error path`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getDomains(any()) } throws ApiException(ApiResult.Error.NoInternet())

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Error.Message>(awaitItem())

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
        coEvery { accountAvailability.getDomains(any()) } returns listOf(
            "protonmail.com",
            "protonmail.ch"
        )
        coEvery {
            accountAvailability.checkUsername(
                userId = any(),
                username = any(),
                metricData = any()
            )
        } returns Unit

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Data.Domains>(awaitItem())
            val state = awaitItem()
            assertIs<ChooseAddressViewModel.ChooseAddressState.Data.UsernameProposal>(state)
            assertEquals("testemail", state.username)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `username unavailable`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getUser(any()) } returns user
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains(any()) } returns listOf(
            "protonmail.com",
            "protonmail.ch"
        )
        coEvery {
            accountAvailability.checkUsername(
                userId = any(),
                username = any(),
                metricData = any()
            )
        } coAnswers {
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
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Data.Domains>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Idle>(awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `username unavailable then available`() = coroutinesTest {
        // GIVEN
        coEvery { accountAvailability.getUser(any()) } returns user
        coEvery { user.email } returns "testemail@test.com"
        coEvery { user.keys } returns emptyList()
        coEvery { accountAvailability.getDomains(any()) } returns listOf(
            "protonmail.com",
            "protonmail.ch"
        )
        coEvery {
            accountAvailability.checkUsername(
                userId = any(),
                username = any(),
                metricData = any()
            )
        } coAnswers {
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
        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any(),
                internalAddressDomain = any(),
                subscribeMetricData = any(),
                userCheckMetricData = any(),
                unlockUserMetricData = any()
            )
        } returns PostLoginAccountSetup.Result.UserUnlocked(userId)

        flowTest(viewModel.chooseAddressState) {
            // WHEN
            viewModel.setUserId(userId)

            // THEN
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Data.Domains>(awaitItem())
            assertIs<ChooseAddressViewModel.ChooseAddressState.Idle>(awaitItem())

            coEvery {
                accountAvailability.checkUsername(
                    userId = any(),
                    username = any(),
                    metricData = any()
                )
            } returns Unit
            viewModel.submit(
                userId = userId,
                username = "new-username",
                password = "password",
                domain = "new-domain",
                isTwoPassModeNeeded = false
            )
            assertIs<ChooseAddressViewModel.ChooseAddressState.Processing>(awaitItem())

            val state = awaitItem()
            assertIs<ChooseAddressViewModel.ChooseAddressState.AccountSetupResult>(state)
            val postLoginSetupResult =
                assertIs<PostLoginAccountSetup.Result.UserUnlocked>(state.result)
            assertEquals(userId, postLoginSetupResult.userId)

            cancelAndConsumeRemainingEvents()
        }
    }
}
