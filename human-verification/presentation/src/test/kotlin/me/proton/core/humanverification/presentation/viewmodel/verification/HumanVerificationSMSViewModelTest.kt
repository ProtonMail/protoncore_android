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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.exception.NoCountriesException
import me.proton.core.humanverification.domain.usecase.MostUsedCountryCode
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToPhoneDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
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
    fun `most used calling code returns success`() = runBlockingTest {
        coEvery { mostUsedUseCase.invoke() } returns flowOf(0)
        assertIs<ViewState.Success<Int>>(viewModel.mostUsedCallingCode.awaitNext())
    }

    @Test
    fun `most used calling code returns correct data`() = runBlockingTest {
        coEvery { mostUsedUseCase.invoke() } returns flowOf(1)
        viewModel.mostUsedCallingCode.awaitNext()
        assertEquals(1, viewModel.mostUsedCallingCode.awaitData())
    }

    @Test
    fun `use case throws no countries exception`() = runBlockingTest {
        coEvery { mostUsedUseCase.invoke() } answers {
            throw NoCountriesException()
        }

        assertIs<ViewState.Error>(viewModel.mostUsedCallingCode.awaitNext())
    }

    @Test
    fun `send verification code to phone number success`() = runBlockingTest {
        coEvery { mostUsedUseCase.invoke() } returns flowOf(0)
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns VerificationResult.Success
        viewModel.sendVerificationCodeToDestination(sessionId, "+0", "123456789")
        assertIs<ViewState.Success<Boolean>>(viewModel.verificationCodeStatus.awaitNext())
    }

    @Test
    fun `send verification code to phone number invalid`() = runBlockingTest {
        // given
        coEvery { mostUsedUseCase.invoke() } returns flowOf(0)
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns VerificationResult.Success

        // when
        viewModel.sendVerificationCodeToDestination(sessionId, "", "")
        val result = viewModel.validation.awaitNext()

        // then
        assertIs<ViewState.Error>(result)
        val throwable = (result as ViewState.Error).throwable
        assertIs<EmptyDestinationException>(throwable)
        assertEquals("Destination phone number:  is invalid.", throwable.message)
    }
}
