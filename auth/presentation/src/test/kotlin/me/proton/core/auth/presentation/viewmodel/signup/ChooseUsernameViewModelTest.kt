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
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.presentation.viewmodel.signup.ChooseUsernameViewModel.State
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotalV1
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotalV1
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChooseUsernameViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    private lateinit var accountAvailability: AccountAvailability

    @MockK(relaxed = true)
    private lateinit var domainRepository: DomainRepository

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: ChooseUsernameViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        accountAvailability = AccountAvailability(userRepository, domainRepository, observabilityManager)
    }

    @Test
    fun `check username success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { userRepository.checkUsernameAvailable(testUsername) } returns Unit
        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkUsername(testUsername)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username error`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { userRepository.checkUsernameAvailable(testUsername) } throws ApiException(
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
        viewModel = ChooseUsernameViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkUsername(testUsername)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Error.Message>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `get domains is initially called, before checkUsername`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { userRepository.checkUsernameAvailable(testUsername) } returns Unit
        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkUsername(testUsername)
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            domainRepository.getAvailableDomains()
            userRepository.checkUsernameAvailable(testUsername)
        }
    }

    @Test
    fun `observability data is enqueued`() = coroutinesTest {
        // GIVEN
        coEvery { userRepository.checkUsernameAvailable(any()) } returns Unit

        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability)
        viewModel.checkUsername("test-username").join()

        // THEN
        val fetchDomainsEventSlot = slot<SignupFetchDomainsTotalV1>()
        val usernameAvailabilityEventSlot = slot<SignupUsernameAvailabilityTotalV1>()

        coVerify {
            observabilityManager.enqueue(capture(fetchDomainsEventSlot), any())
            observabilityManager.enqueue(capture(usernameAvailabilityEventSlot), any())
        }

        assertEquals(HttpApiStatus.http2xx, fetchDomainsEventSlot.captured.Labels.status)
        assertEquals(HttpApiStatus.http2xx, usernameAvailabilityEventSlot.captured.Labels.status)
    }
}
