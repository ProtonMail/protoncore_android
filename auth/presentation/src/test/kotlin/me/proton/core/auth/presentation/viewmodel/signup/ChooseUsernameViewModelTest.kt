/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.UsernameAvailabilityStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
        accountAvailability = AccountAvailability(userRepository, domainRepository)
    }

    @Test
    fun `check username success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { userRepository.checkUsernameAvailable(any(), testUsername) } returns Unit
        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability, observabilityManager)
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
        coEvery { userRepository.checkUsernameAvailable(any(), testUsername) } throws ApiException(
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
        viewModel = ChooseUsernameViewModel(accountAvailability, observabilityManager)
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
        coEvery { userRepository.checkUsernameAvailable(any(), testUsername) } returns Unit
        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability, observabilityManager)
        viewModel.state.test {
            viewModel.checkUsername(testUsername)
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            domainRepository.getAvailableDomains(any())
            userRepository.checkUsernameAvailable(any(), testUsername)
        }
    }

    @Test
    fun `observability data is enqueued`() = coroutinesTest {
        // GIVEN
        val fetchDomainsEventSlot = slot<SignupFetchDomainsTotal>()
        val usernameAvailabilityEventSlot = slot<SignupUsernameAvailabilityTotal>()

        coEvery { domainRepository.getAvailableDomains(any()) } coAnswers {
            result("getAvailableDomains") { listOf("protonmail.com", "protonmail.ch") }
        }
        coEvery { userRepository.checkUsernameAvailable(any(), any()) } coAnswers {
            result("checkUsernameAvailable") { /* Unit */ }
        }

        // WHEN
        viewModel = ChooseUsernameViewModel(accountAvailability, observabilityManager)
        viewModel.checkUsername("test-username").join()

        // THEN
        coVerify {
            observabilityManager.enqueue(capture(fetchDomainsEventSlot), any())
            observabilityManager.enqueue(capture(usernameAvailabilityEventSlot), any())
        }

        assertEquals(HttpApiStatus.http2xx, fetchDomainsEventSlot.captured.Labels.status)
        assertEquals(UsernameAvailabilityStatus.http2xx, usernameAvailabilityEventSlot.captured.Labels.status)
    }
}
