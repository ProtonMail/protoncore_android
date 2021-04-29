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

package me.proton.core.user.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.UserVerificationTokenType
import me.proton.core.user.domain.entity.VerificationResult
import me.proton.core.user.domain.repository.UserValidationRepository
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckCreationTokenValidityTest {

    private val remoteRepository = mockk<UserValidationRepository>()

    private val testToken = "test-token"
    private val testTokenType = 1

    @Test
    fun `code verification with email success`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "email", 1) } returns VerificationResult.Success
        val result = useCase.invoke(testToken, UserVerificationTokenType.EMAIL.tokenTypeValue, testTokenType)

        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `code verification with email error`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "email", 1) } returns VerificationResult.Error("test error")
        val result = useCase.invoke(testToken, UserVerificationTokenType.EMAIL.tokenTypeValue, testTokenType)

        assertTrue(result is VerificationResult.Error)
    }

    @Test
    fun `code verification with sms success`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "sms", 1) } returns VerificationResult.Success
        val result = useCase.invoke(testToken, UserVerificationTokenType.SMS.tokenTypeValue, testTokenType)

        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `code verification with sms error`() = runBlockingTest {
        val useCase = CheckCreationTokenValidity(remoteRepository)
        coEvery { remoteRepository.checkCreationTokenValidity(testToken, "sms", 1) } returns VerificationResult.Error("test error")
        val result = useCase.invoke(testToken, UserVerificationTokenType.SMS.tokenTypeValue, testTokenType)

        assertTrue(result is VerificationResult.Error)
    }
}
