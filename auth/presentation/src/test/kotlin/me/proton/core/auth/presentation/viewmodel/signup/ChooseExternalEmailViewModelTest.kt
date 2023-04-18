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
import me.proton.core.auth.presentation.viewmodel.signup.ChooseExternalEmailViewModel.State
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupEmailAvailabilityTotal
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChooseExternalEmailViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    private lateinit var accountAvailability: AccountAvailability

    @MockK(relaxed = true)
    private lateinit var domainRepository: DomainRepository

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: ChooseExternalEmailViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        accountAvailability = AccountAvailability(userRepository, domainRepository, observabilityManager)
    }

    @Test
    fun `check email success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { userRepository.checkExternalEmailAvailable(testEmail) } returns Unit
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            val item = awaitItem() as State.Success
            assertEquals(testEmail, item.email)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check email error`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { userRepository.checkExternalEmailAvailable(testEmail) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "email not available"
                )
            )
        )
        // WHEN
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Error.Message>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check proton domain, switch to internal`() = coroutinesTest {
        // GIVEN
        val testUsername = "username"
        val testDomain = "proton.me"
        val testEmail = "$testUsername@$testDomain"
        coEvery { domainRepository.getAvailableDomains() } returns listOf("proton.me", "proton.ch")
        // WHEN
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.SwitchInternal>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check proton domain, don't switch to internal`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { domainRepository.getAvailableDomains() } returns listOf("proton.me", "proton.ch")
        coEvery { userRepository.checkExternalEmailAvailable(testEmail) } returns Unit
        // WHEN
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            // THEN
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `get domains is initially called, before checkExternalEmail`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { userRepository.checkExternalEmailAvailable(testEmail) } returns Unit
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.state.test {
            // WHEN
            viewModel.checkExternalEmail(testEmail)
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        // THEN
        coVerify(ordering = Ordering.ORDERED) {
            accountAvailability.getDomains()
            accountAvailability.checkExternalEmail(testEmail)
        }
    }

    @Test
    fun `observability data is enqueued`() = coroutinesTest {
        // GIVEN
        coEvery { userRepository.checkExternalEmailAvailable(any()) } returns Unit

        // WHEN
        viewModel = ChooseExternalEmailViewModel(accountAvailability)
        viewModel.checkExternalEmail("username@email.text").join()

        // THEN
        val fetchDomainsEventSlot = slot<SignupFetchDomainsTotal>()
        val usernameAvailabilityEventSlot = slot<SignupEmailAvailabilityTotal>()

        coVerify {
            observabilityManager.enqueue(capture(fetchDomainsEventSlot), any())
            observabilityManager.enqueue(capture(usernameAvailabilityEventSlot), any())
        }

        assertEquals(HttpApiStatus.http2xx, fetchDomainsEventSlot.captured.Labels.status)
        assertEquals(HttpApiStatus.http2xx, usernameAvailabilityEventSlot.captured.Labels.status)
    }
}
