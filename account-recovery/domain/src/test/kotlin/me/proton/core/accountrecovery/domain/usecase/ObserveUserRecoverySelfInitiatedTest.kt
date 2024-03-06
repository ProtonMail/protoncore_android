/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.accountrecovery.domain.usecase

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.UserRecovery
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveUserRecoverySelfInitiatedTest {

    private val otherUserId = UserId("userId")
    private val otherSessionId = SessionId("sessionId")

    private val otherSession: Session = mockk<Session.Authenticated> {
        every { this@mockk.sessionId } returns otherSessionId
    }

    private val initiatedUserId = UserId("initiatedUserId")
    private val initiatedSessionId = SessionId("initiatedSessionId")

    private val initiatedSession: Session = mockk<Session.Authenticated> {
        every { this@mockk.sessionId } returns initiatedSessionId
    }

    private val accountManager: AccountManager = mockk {
        coEvery { this@mockk.getSessions() } returns flowOf(listOf(otherSession, initiatedSession))
    }

    private val initiatedRecovery: UserRecovery = mockk {
        every { sessionId } returns initiatedSessionId
    }
    private val observeUserRecovery: ObserveUserRecovery = mockk {
        coEvery { this@mockk.invoke(otherUserId) } returns flowOf(initiatedRecovery)
        coEvery { this@mockk.invoke(initiatedUserId) } returns flowOf(initiatedRecovery)
    }

    private lateinit var useCase: ObserveUserRecoverySelfInitiated

    @Before
    fun beforeEveryTest() {
        useCase = ObserveUserRecoverySelfInitiated(accountManager, observeUserRecovery)
    }

    @Test
    fun recoveryNull() = runTest {
        // GIVEN
        coEvery { observeUserRecovery.invoke(otherUserId) } returns flowOf(null)

        // WHEN
        useCase.invoke(otherUserId).test {
            // THEN
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun recoveryDoNotMatch() = runTest {
        // GIVEN
        coEvery { accountManager.getSessions() } returns flowOf(listOf(otherSession))

        // WHEN
        useCase.invoke(otherUserId).test {
            // THEN
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun recoveryMatch() = runTest {
        // GIVEN
        coEvery { accountManager.getSessions() } returns flowOf(listOf(otherSession, initiatedSession))

        // WHEN
        useCase.invoke(otherUserId).test {
            // THEN
            assertTrue(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
