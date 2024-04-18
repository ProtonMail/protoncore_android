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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import me.proton.core.auth.domain.usecase.AccountAvailability
import me.proton.core.auth.domain.usecase.GetPrimaryUser
import me.proton.core.auth.presentation.viewmodel.signup.ChooseExternalEmailViewModel.State
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupEmailAvailabilityTotal
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChooseExternalEmailViewModelTest : ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest() {
    private lateinit var accountAvailability: AccountAvailability

    @MockK(relaxed = true)
    private lateinit var domainRepository: DomainRepository

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var getPrimaryUser: GetPrimaryUser

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: ChooseExternalEmailViewModel

    val userId = UserId("123")
    private val user = User(
        userId = userId,
        email = null,
        name = "test username",
        displayName = null,
        currency = "test-curr",
        credit = 0,
        createdAtUtc = 1000L,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList(),
        type = Type.Proton
    )

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { getPrimaryUser() } returns user
        accountAvailability = AccountAvailability(userRepository, domainRepository, getPrimaryUser)
        viewModel = ChooseExternalEmailViewModel(accountAvailability, observabilityManager)
    }

    @Test
    fun `check email success`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { userRepository.checkExternalEmailAvailable(null, testEmail) } returns Unit
        // WHEN
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
        coEvery { userRepository.checkExternalEmailAvailable(null, testEmail) } throws ApiException(
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
        coEvery { domainRepository.getAvailableDomains(any()) } returns listOf(
            "proton.me",
            "proton.ch"
        )
        // WHEN
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
        coEvery { domainRepository.getAvailableDomains(any()) } returns listOf(
            "proton.me",
            "proton.ch"
        )
        coEvery { userRepository.checkExternalEmailAvailable(null, testEmail) } returns Unit
        // WHEN
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
        coEvery { userRepository.checkExternalEmailAvailable(any(), testEmail) } returns Unit

        // WHEN
        viewModel.state.test {
            viewModel.checkExternalEmail(testEmail)
            assertIs<State.Idle>(awaitItem())
            assertIs<State.Processing>(awaitItem())
            assertIs<State.Success>(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `observability data is enqueued`() = coroutinesTest {
    // GIVEN
        coEvery { domainRepository.getAvailableDomains(any()) } coAnswers {
            result("getAvailableDomains") { listOf("domain") }
        }
        coEvery { userRepository.checkExternalEmailAvailable(any(), any()) } coAnswers {
            result("checkExternalEmailAvailable") { /* Unit */ }
        }

        // WHEN
        viewModel.checkExternalEmail("username@email.text").join()

        // THEN
        val fetchDomainsEventSlot = slot<SignupFetchDomainsTotal>()
        val emailAvailabilityEventSlot = slot<SignupEmailAvailabilityTotal>()

        coVerify {
            observabilityManager.enqueue(capture(fetchDomainsEventSlot), any())
            observabilityManager.enqueue(capture(emailAvailabilityEventSlot), any())
        }

        assertEquals(HttpApiStatus.http2xx, fetchDomainsEventSlot.captured.Labels.status)
        assertEquals(HttpApiStatus.http2xx, emailAvailabilityEventSlot.captured.Labels.status)
    }
}
