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
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.DefaultCountry
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToPhoneDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class HumanVerificationSMSViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val defaultCountry = mockk<DefaultCountry>()
    private val sendToPhoneDestinationUseCase = mockk<SendVerificationCodeToPhoneDestination>()

    private val country: Country = Country(
        code = "code",
        name = "name",
        callingCode = 0
    )

    private val sessionId: SessionId = SessionId("id")

    private val viewModel by lazy {
        HumanVerificationSMSViewModel(
            defaultCountry,
            sendToPhoneDestinationUseCase
        )
    }

    @Test
    fun `calling code returns success`() = coroutinesTest {
        coEvery { defaultCountry.invoke() } returns country
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(awaitItem())
            assertIs<ViewModelResult.Processing>(awaitItem())
            assertIs<ViewModelResult.Success<Int>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calling code returns correct data`() = coroutinesTest {
        coEvery { defaultCountry.invoke() } returns country
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(awaitItem())
            assertIs<ViewModelResult.Processing>(awaitItem())
            assertEquals(0, (awaitItem() as ViewModelResult.Success).value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `use case throws no countries exception`() = coroutinesTest {
        coEvery { defaultCountry.invoke() } returns null
        viewModel.countryCallingCode.test() {
            viewModel.getCountryCallingCode()
            assertIs<ViewModelResult.None>(awaitItem())
            assertIs<ViewModelResult.Processing>(awaitItem())
            assertIs<ViewModelResult.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send verification code to phone number success`() = coroutinesTest {
        coEvery { defaultCountry.invoke() } returns country
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns Unit
        viewModel.sendVerificationCodeToDestination(sessionId, "+0", "123456789")
        viewModel.verificationCodeStatus.test(timeout = Duration.seconds(2)) {
            assertIs<ViewModelResult.Success<Boolean>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send verification code to phone number invalid`() = coroutinesTest {
        // given
        coEvery { defaultCountry.invoke() } returns country
        coEvery { sendToPhoneDestinationUseCase.invoke(any(), any()) } returns Unit

        // when
        viewModel.sendVerificationCodeToDestination(sessionId, "", "")
        // then
        viewModel.validation.test(timeout = Duration.seconds(2)) {
            val result = awaitItem() as ViewModelResult.Error
            assertIs<IllegalArgumentException>(result.throwable)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
