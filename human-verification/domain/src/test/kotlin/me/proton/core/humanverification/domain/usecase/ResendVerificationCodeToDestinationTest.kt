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

package me.proton.core.humanverification.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.InvalidValidationOptionException
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Tests the ResendVerificationCodeToDestination use case.
 *
 * @author Dino Kadrikj.
 */
class ResendVerificationCodeToDestinationTest {

    private val remoteRepository = mockk<HumanVerificationRemoteRepository>()

    @Rule
    @JvmField
    var thrown: ExpectedException = ExpectedException.none()

    private val sessionId: SessionId = SessionId("id")
    private val testEmail = "test@protonmail.com"
    private val testPhoneNumber = "+123456789"

    @Test
    fun `send verification token with token type email`() = runBlockingTest {
        val useCase = ResendVerificationCodeToDestination(remoteRepository)
        coEvery { remoteRepository.sendVerificationCodePhoneNumber(any(), any()) } returns VerificationResult.Success
        coEvery { remoteRepository.sendVerificationCodeEmailAddress(any(), any()) } returns VerificationResult.Success
        val result = useCase.invoke(sessionId, TokenType.EMAIL, testEmail)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send verification token with token type sms`() = runBlockingTest {
        val useCase = ResendVerificationCodeToDestination(remoteRepository)
        coEvery { remoteRepository.sendVerificationCodePhoneNumber(any(), any()) } returns VerificationResult.Success
        coEvery { remoteRepository.sendVerificationCodeEmailAddress(any(), any()) } returns VerificationResult.Success
        val result = useCase.invoke(sessionId, TokenType.SMS, testPhoneNumber)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send verification token with token type invalid option`() = runBlockingTest {
        val useCase = ResendVerificationCodeToDestination(remoteRepository)
        coEvery { remoteRepository.sendVerificationCodePhoneNumber(any(), any()) } returns VerificationResult.Success
        coEvery { remoteRepository.sendVerificationCodeEmailAddress(any(), any()) } returns VerificationResult.Success

        thrown.expect(InvalidValidationOptionException::class.java)
        thrown.expectMessage("Invalid verification type selected")

        useCase.invoke(sessionId, TokenType.CAPTCHA, testPhoneNumber)
    }
}
