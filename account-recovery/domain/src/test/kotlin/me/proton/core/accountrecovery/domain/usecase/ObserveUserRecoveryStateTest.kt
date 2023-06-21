/*
 * Copyright (c) 2022 Proton Technologies AG
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
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserRecovery
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ObserveUserRecoveryStateTest {

    private val testUserId = UserId("test-user-id")
    private val testUser = User(
        userId = testUserId,
        email = null,
        name = "test-username",
        displayName = null,
        currency = "CHF",
        credit = 0,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = UserRecovery(
            state = IntEnum(1, UserRecovery.State.Grace),
            startTime = 1L,
            endTime = 10L,
            sessionId = SessionId("test-session-id"),
            reason = UserRecovery.Reason.Authentication
    ),
        keys = emptyList()
    )
    private val testUserNullRecovery = testUser.copy(recovery = null)

    private val user = MutableStateFlow(testUserNullRecovery)
    private val userManager = mockk<UserManager>(relaxed = true) {
        coEvery { this@mockk.getUser(testUserId, any()) } returns user.value
        coEvery { this@mockk.observeUser(testUserId, any()) } returns user
    }

    private lateinit var useCase: ObserveUserRecoveryState

    @Before
    fun beforeEveryTest() {
        useCase = ObserveUserRecoveryState(userManager)
    }

    @Test
    fun `null user recovery returns state none`() = runTest {
        // WHEN
        useCase.invoke(testUserId, true).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())
        }
    }

    @Test
    fun `non null user updated after null`() = runTest {
        // WHEN
        useCase.invoke(testUserId, true).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())

            user.emit(testUser)
            assertEquals(UserRecovery.State.Grace, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `non null user update after null becomes null again`() = runTest {
        // WHEN
        useCase.invoke(testUserId, true).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())

            user.emit(testUser)
            assertEquals(UserRecovery.State.Grace, awaitItem())

            user.emit(testUserNullRecovery)
            assertEquals(UserRecovery.State.None, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `non null user update after null becomes null again no refresh`() = runTest {
        // WHEN
        useCase.invoke(testUserId, false).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())

            user.emit(testUser)
            assertEquals(UserRecovery.State.Grace, awaitItem())

            user.emit(testUserNullRecovery)
            assertEquals(UserRecovery.State.None, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `non null user update after null becomes null again ResetPassword state`() = runTest {
        // WHEN
        useCase.invoke(testUserId, false).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())

            user.emit(testUser.copy(
                recovery = UserRecovery(
                    state = IntEnum(1, UserRecovery.State.Insecure),
                    startTime = 1L,
                    endTime = 10L,
                    sessionId = SessionId("test-session-id"),
                    reason = UserRecovery.Reason.Authentication
                )
            ))
            assertEquals(UserRecovery.State.Insecure, awaitItem())

            user.emit(testUserNullRecovery)
            assertEquals(UserRecovery.State.None, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `non null user update after null becomes null again Cancelled state`() = runTest {
        // WHEN
        useCase.invoke(testUserId, false).test {
            // THEN
            assertEquals(UserRecovery.State.None, awaitItem())

            user.emit(testUser.copy(
                recovery = UserRecovery(
                    state = IntEnum(1, UserRecovery.State.Cancelled),
                    startTime = 1L,
                    endTime = 10L,
                    sessionId = SessionId("test-session-id"),
                    reason = UserRecovery.Reason.Authentication
                )
            ))
            assertEquals(UserRecovery.State.Cancelled, awaitItem())

            user.emit(testUserNullRecovery)
            assertEquals(UserRecovery.State.None, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}