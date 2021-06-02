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
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Rule
import org.junit.Test

class HumanVerificationEnterCodeViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val resendVerificationCodeToDestination = mockk<ResendVerificationCodeToDestination>()

    private val sessionId: SessionId = SessionId("id")
    private val testEmail = "test@protonmail.com"
    private val errorResponse = "test error"

    private val viewModel by lazy {
        HumanVerificationEnterCodeViewModel(
            resendVerificationCodeToDestination
        )
    }

    @Test
    fun `resend token success`() = coroutinesTest {
        // given
        val tokenType = TokenType.EMAIL
        viewModel.tokenType = tokenType
        viewModel.destination = testEmail
        coEvery {
            resendVerificationCodeToDestination.invoke(
                sessionId = any(),
                tokenType = any(),
                destination = any()
            )
        } returns Unit
        // when
        viewModel.resendCode(sessionId)
        // then
        viewModel.verificationCodeResendStatus.test {
            assertIs<ViewModelResult.Success<Boolean>>(expectItem())
        }
    }

    @Test
    fun `resend token failure`() = coroutinesTest {
        // given
        val tokenType = TokenType.EMAIL
        viewModel.tokenType = tokenType
        viewModel.destination = testEmail
        coEvery {
            resendVerificationCodeToDestination.invoke(
                sessionId = any(),
                tokenType = any(),
                destination = any()
            )
        } throws IllegalArgumentException(errorResponse)
        // when
        viewModel.resendCode(sessionId)
        // then
        viewModel.verificationCodeResendStatus.test {
            val result = expectItem() as ViewModelResult.Error
            assertIs<IllegalArgumentException>(result.throwable)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
