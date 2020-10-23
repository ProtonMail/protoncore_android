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
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.humanverification.presentation.exception.VerificationCodeSendingException
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.coroutinesTest
import org.junit.Rule
import org.junit.Test
import studio.forface.viewstatestore.ViewState
import kotlin.test.assertEquals

/**
 * @author Dino Kadrikj.
 */
class HumanVerificationEmailViewModelTest : CoroutinesTest by coroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val useCase = mockk<SendVerificationCodeToEmailDestination>()

    private val sessionId: SessionId = SessionId("id")
    private val testEmail = "test@protonmail.com"
    private val errorResponse = "test error"

    private val viewModel by lazy {
        HumanVerificationEmailViewModel(useCase)
    }

    @Test
    fun `send verification code to email address success`() = runBlockingTest {
        coEvery {
            useCase.invoke(any(), any())
        } returns VerificationResult.Success

        viewModel.sendVerificationCode(sessionId, testEmail)
        assertIs<ViewState.Success<Boolean>>(viewModel.verificationCodeStatus.awaitNext())
    }

    @Test
    fun `send verification code to email address error`() = runBlockingTest {
        coEvery {
            useCase.invoke(any(), any())
        } returns VerificationResult.Error(errorResponse)

        viewModel.sendVerificationCode(sessionId, testEmail)
        val result = viewModel.verificationCodeStatus.awaitNext()
        assertIs<ViewState.Error>(result)
        val throwable = (result as ViewState.Error).throwable
        assertIs<VerificationCodeSendingException>(throwable)
    }


    @Test
    fun `send verification code to email address invalid`() = runBlockingTest {
        coEvery {
            useCase.invoke(any(), any())
        } returns VerificationResult.Success

        viewModel.sendVerificationCode(sessionId, "")
        val result = viewModel.validation.awaitNext()
        assertIs<ViewState.Error>(result)
        val throwable = (result as ViewState.Error).throwable
        assertEquals(
            "Destination email:  is invalid.",
            (throwable as EmptyDestinationException).message
        )
    }

}
