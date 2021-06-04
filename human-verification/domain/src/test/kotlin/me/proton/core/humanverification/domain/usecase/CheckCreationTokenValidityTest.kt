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

package me.proton.core.humanverification.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.user.domain.entity.CreateUserType
import org.junit.Test

class CheckCreationTokenValidityTest {

    private val remoteRepository = mockk<UserVerificationRepository>()

    private val testToken = "test-token"
    private val testTokenType = CreateUserType.Normal

    @Test
    fun `code verification with email success`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "email", 1) } returns Unit

        useCase.invoke(testToken, TokenType.EMAIL.value, testTokenType)
    }

    @Test(expected = Exception::class)
    fun `code verification with email error`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "email", 1) } throws Exception("test error")

        useCase.invoke(testToken, TokenType.EMAIL.value, testTokenType)
    }

    @Test
    fun `code verification with sms success`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "sms", 1) } returns Unit

        useCase.invoke(testToken, TokenType.SMS.value, testTokenType)
    }

    @Test(expected = Exception::class)
    fun `code verification with sms error`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "sms", 1) } throws Exception("test error")

        useCase.invoke(testToken, TokenType.SMS.value, testTokenType)
    }
}
