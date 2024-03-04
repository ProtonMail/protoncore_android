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
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.server.ServerClock
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.isCredentialLess
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostLoginLessAccountSetupTest {
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var userCheck: PostLoginAccountSetup.UserCheck
    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var user: User
    private lateinit var sessionId: SessionId
    private lateinit var serverClock: ServerClock

    private lateinit var tested: PostLoginLessAccountSetup

    private val testUserId: UserId = UserId("user-id")

    @Before
    fun setUp() {
        accountWorkflowHandler = mockk()

        user = mockk()
        sessionId = mockk()
        userCheck = mockk {
            coEvery { this@mockk.invoke(any()) } returns PostLoginAccountSetup.UserCheckResult.Success
        }
        userManager = mockk {
            coJustRun { addUser(any(), any()) }
            coEvery { getUser(any(), any()) } returns user
        }
        sessionManager = mockk {
            coEvery { getSessionId(any()) } returns sessionId
            coEvery { refreshScopes(any()) } returns Unit
        }
        serverClock = mockk {
            every { getCurrentTimeUTC() } returns Instant.ofEpochMilli(1)
        }

        tested = PostLoginLessAccountSetup(
            accountWorkflowHandler,
            userCheck,
            userManager,
            sessionManager,
            serverClock
        )
    }

    @Test
    fun `user check error`() = runTest {
        val setupError = mockk<PostLoginAccountSetup.UserCheckResult.Error>()
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountDisabled(any()) }
        coEvery { userCheck.invoke(any()) } returns setupError

        val result = tested.invoke(sessionInfo.userId)
        assertTrue(result is PostLoginAccountSetup.UserCheckResult.Error)
        coVerify { accountWorkflowHandler.handleAccountDisabled(testUserId) }
    }

    @Test
    fun `user check success`() = runTest {
        val setupSuccess = mockk<PostLoginAccountSetup.UserCheckResult.Success>()
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { userCheck.invoke(any()) } returns setupSuccess

        val result = tested.invoke(sessionInfo.userId)
        assertTrue(result is PostLoginAccountSetup.UserCheckResult.Success)
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }

        val userSlot = slot<User>()
        coVerify { userManager.addUser(capture(userSlot), emptyList()) }
        assertEquals(sessionInfo.userId, userSlot.captured.userId)
        assertEquals(1L, userSlot.captured.createdAtUtc)
        assertTrue { userSlot.captured.isCredentialLess() }
    }

    private fun mockSessionInfo(
        secondFactorNeeded: Boolean = false,
        temporaryPassword: Boolean = false
    ) = mockk<SessionInfo> {
        every { userId } returns testUserId
        every { isSecondFactorNeeded } returns secondFactorNeeded
        every { isTwoPassModeNeeded } returns false
        every { this@mockk.temporaryPassword } returns temporaryPassword
    }
}
