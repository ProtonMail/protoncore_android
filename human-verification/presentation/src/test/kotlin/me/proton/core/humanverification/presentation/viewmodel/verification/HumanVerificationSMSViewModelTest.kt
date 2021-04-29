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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import me.proton.core.country.domain.usecase.MostUsedCountryCode
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import me.proton.core.user.domain.entity.VerificationResult
import me.proton.core.user.domain.exception.EmptyDestinationException
import me.proton.core.user.domain.usecase.SendVerificationCodeToPhoneDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.seconds

class HumanVerificationSMSViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val mostUsedUseCase = mockk<MostUsedCountryCode>()
    private val sendToPhoneDestinationUseCase = mockk<SendVerificationCodeToPhoneDestination>()

    private val sessionId: SessionId = SessionId("id")

    private val viewModel by lazy {
        HumanVerificationSMSViewModel(
            mostUsedUseCase,
            sendToPhoneDestinationUseCase
        )
    }

    @Test
    fun `most used calling code returns success`() = coroutinesTest {
        coEvery { mostUsedUseCase.invoke() } returns 0
        viewModel.mostUsedCallingCode.test() {
            viewModel.getMostUsedCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            assertIs<ViewModelResult.Success<Int>>(expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `most used calling code returns correct data`() = coroutinesTest {
        coEvery { mostUsedUseCase.invoke() } returns 1
        viewModel.mostUsedCallingCode.test() {
            viewModel.getMostUsedCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            assertEquals(1, (expectItem() as ViewModelResult.Success).value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `use case throws no countries exception`() = coroutinesTest {
        coEvery { mostUsedUseCase.invoke() } returns null
        viewModel.mostUsedCallingCode.test() {
            viewModel.getMostUsedCallingCode()
            assertIs<ViewModelResult.None>(expectItem())
            assertIs<ViewModelResult.Processing>(expectItem())
            assertIs<ViewModelResult.Error>(expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send verification code to phone number success`() = coroutinesTest {
        coEvery { mostUsedUseCase.invoke() } returns 0
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns VerificationResult.Success
        viewModel.sendVerificationCodeToDestination(sessionId, "+0", "123456789")
        viewModel.verificationCodeStatus.test(timeout = 2.seconds) {
            assertIs<ViewModelResult.Success<Boolean>>(expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `send verification code to phone number invalid`() = coroutinesTest {
        // given
        coEvery { mostUsedUseCase.invoke() } returns 0
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns VerificationResult.Success

        // when
        viewModel.sendVerificationCodeToDestination(sessionId, "", "")
        // then
        viewModel.validation.test(timeout = 2.seconds) {
            val result = expectItem() as ViewModelResult.Error
            assertIs<EmptyDestinationException>(result.throwable)
            assertEquals("Destination phone number:  is invalid.", result.throwable?.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
