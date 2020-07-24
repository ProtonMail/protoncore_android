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
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.usecase.ResendVerificationCodeToDestination
import me.proton.core.humanverification.domain.usecase.VerifyCode
import me.proton.core.humanverification.presentation.exception.TokenCodeVerificationException
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class HumanVerificationEnterCodeViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val resendVerificationCodeToDestination = mockk<ResendVerificationCodeToDestination>()
    private val verifyCode = mockk<VerifyCode>()

    private val testEmail = "test@protonmail.com"
    private val errorResponse = "test error"

    private val viewModel by lazy {
        HumanVerificationEnterCodeViewModel(
            resendVerificationCodeToDestination,
            verifyCode
        )
    }

    @Test
    fun `verify code success`() = runBlockingTest {
        val tokenType = TokenType.EMAIL
        val token = "testToken"
        coEvery {
            verifyCode.invoke(
                tokenType =
                any(), verificationCode =
                any()
            )
        } returns VerificationResult.Success
        viewModel.verifyTokenCode(tokenType, token)
        assertIs<ViewState.Success<Boolean>>(viewModel.codeVerificationResult.awaitNext())
    }

    @Test
    fun `verify code fails correctly`() = runBlockingTest {
        // given
        val tokenType = TokenType.EMAIL
        val token = "testToken"
        coEvery {
            verifyCode.invoke(
                tokenType = any(),
                verificationCode = any()
            )
        } returns VerificationResult.Error(errorResponse)
        // when
        viewModel.verifyTokenCode(tokenType, token)
        val result = viewModel.codeVerificationResult.awaitNext()
        // then
        assertIs<ViewState.Error>(result)
        assertIs<TokenCodeVerificationException>((result as ViewState.Error).throwable)
    }

    @Test
    fun `resend token success`() = runBlockingTest {
        // given
        val tokenType = TokenType.EMAIL
        viewModel.tokenType = tokenType
        viewModel.destination = testEmail
        coEvery {
            resendVerificationCodeToDestination.invoke(
                tokenType = any(),
                destination = any()
            )
        } returns VerificationResult.Success
        // when
        viewModel.resendCode()
        // then
        assertIs<ViewState.Success<Boolean>>(viewModel.verificationCodeResendStatus.awaitNext())
    }

    @Test
    fun `resend token failure`() = runBlockingTest {
        // given
        val tokenType = TokenType.EMAIL
        viewModel.tokenType = tokenType
        viewModel.destination = testEmail
        coEvery {
            resendVerificationCodeToDestination.invoke(
                tokenType = any(),
                destination = any()
            )
        } returns VerificationResult.Error(errorResponse)
        // when
        viewModel.resendCode()
        val result = viewModel.verificationCodeResendStatus.awaitNext()
        // then
        assertIs<ViewState.Error>(result)
        assertIs<VerificationCodeSendingException>((result as ViewState.Error).throwable)
    }
}

