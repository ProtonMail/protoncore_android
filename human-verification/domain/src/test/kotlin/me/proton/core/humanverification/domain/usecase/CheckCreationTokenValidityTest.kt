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
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test

class CheckCreationTokenValidityTest {

    private val clientIdProvider = mockk<ClientIdProvider>()
    private val userVerificationRepository = mockk<UserVerificationRepository>()
    private val humanVerificationRepository = mockk<HumanVerificationRepository>()

    private lateinit var useCase: CheckCreationTokenValidity

    private val sessionId = SessionId("sessionId")
    private val testToken = "test-token"

    @Before
    fun before() {
        coEvery { clientIdProvider.getClientId(any()) } returns ClientId.newClientId(sessionId, "cookieSessionId")
        coEvery { humanVerificationRepository.insertHumanVerificationDetails(any()) } returns Unit

        useCase = CheckCreationTokenValidity(
            clientIdProvider,
            userVerificationRepository,
            humanVerificationRepository
        )
    }

    @Test
    fun `code verification with email success`() = runBlockingTest {
        coEvery {
            userVerificationRepository.checkCreationTokenValidity(
                sessionId,
                testToken,
                TokenType.EMAIL
            )
        } returns Unit

        useCase.invoke(sessionId, testToken, TokenType.EMAIL)
    }

    @Test(expected = Exception::class)
    fun `code verification with email error`() = runBlockingTest {
        coEvery {
            userVerificationRepository.checkCreationTokenValidity(sessionId, testToken, TokenType.EMAIL)
        } throws Exception("test error")

        useCase.invoke(sessionId, testToken, TokenType.EMAIL)
    }

    @Test
    fun `code verification with sms success`() = runBlockingTest {
        coEvery {
            userVerificationRepository.checkCreationTokenValidity(
                sessionId,
                testToken,
                TokenType.SMS
            )
        } returns Unit

        useCase.invoke(sessionId, testToken, TokenType.SMS)
    }

    @Test(expected = Exception::class)
    fun `code verification with sms error`() = runBlockingTest {
        coEvery {
            userVerificationRepository.checkCreationTokenValidity(
                sessionId,
                testToken,
                TokenType.SMS
            )
        } throws Exception("test error")

        useCase.invoke(sessionId, testToken, TokenType.SMS)
    }
}
