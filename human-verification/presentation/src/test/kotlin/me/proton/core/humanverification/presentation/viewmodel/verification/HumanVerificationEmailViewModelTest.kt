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
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Rule
import org.junit.Test

class HumanVerificationEmailViewModelTest : CoroutinesTest {

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
    fun `send verification code to email address success`() = coroutinesTest {
        coEvery {
            useCase.invoke(any(), any())
        } returns Unit

        viewModel.sendVerificationCode(sessionId, testEmail)
        viewModel.verificationCodeStatus.test {
            assertIs<ViewModelResult.Success<Boolean>>(awaitItem())
        }
    }

    @Test
    fun `send verification code to email address error`() = coroutinesTest {
        coEvery {
            useCase.invoke(any(), any())
        } throws Exception(errorResponse)

        viewModel.sendVerificationCode(sessionId, testEmail)
        viewModel.verificationCodeStatus.test {
            val result = awaitItem() as ViewModelResult.Error
            assertIs<Exception>(result.throwable)
        }
    }

    @Test
    fun `send verification code to email address invalid`() = coroutinesTest {
        val useCase = SendVerificationCodeToEmailDestination(mockk())
        val viewModel = HumanVerificationEmailViewModel(useCase)

        viewModel.sendVerificationCode(sessionId, "")
        viewModel.verificationCodeStatus.test {
            val result = awaitItem() as ViewModelResult.Error
            assertIs<IllegalArgumentException>(result.throwable)
        }
    }
}
