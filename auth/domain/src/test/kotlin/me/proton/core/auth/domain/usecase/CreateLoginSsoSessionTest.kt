/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test

class CreateLoginSsoSessionTest {

    private val accountWorkflowHandler: AccountWorkflowHandler = mockk(relaxed = true)
    private val performLogin: PerformLoginSso = mockk {
        coEvery { this@mockk.invoke(any(), any()) } returns SessionInfo(
            username = "email",
            accessToken = "access-token",
            tokenType = "token-type",
            scopes = listOf("scope1", "scope2"),
            sessionId = SessionId("session-id"),
            userId = UserId("user-id"),
            refreshToken = "refresh-token",
            eventId = "event-id",
            serverProof = "server-proof",
            localId = 1,
            passwordMode = 0,
            secondFactor = null,
            temporaryPassword = false
        )
    }
    private lateinit var tested: CreateLoginSsoSession

    private val testEmail = "username@domain.com"
    private val testToken = "token"

    @Before
    fun setUp() {
        tested = CreateLoginSsoSession(accountWorkflowHandler, performLogin)
    }

    @Test
    fun `basic create session from IdentityProvider token`() = runTest {
        // WHEN
        tested.invoke(testEmail, testToken, AccountType.External)
        // THEN
        coVerify { performLogin.invoke(testEmail, testToken) }
        coVerify { accountWorkflowHandler.handleSession(any(), any()) }
    }
}
