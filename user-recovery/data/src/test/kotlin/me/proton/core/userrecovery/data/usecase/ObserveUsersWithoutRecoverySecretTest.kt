/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.userrecovery.data.mock.mockUser
import me.proton.core.userrecovery.data.mock.mockUserKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveUsersWithoutRecoverySecretTest {
    @MockK
    private lateinit var observeUserDeviceRecovery: ObserveUserDeviceRecovery

    private lateinit var tested: ObserveUsersWithoutRecoverySecret

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObserveUsersWithoutRecoverySecret(observeUserDeviceRecovery)
    }

    @Test
    fun `user without recovery secret`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val privateKey = mockk<PrivateKey> {
            every { isPrimary } returns true
        }
        val user = mockUser(userId, listOf(mockUserKey(testRecoverySecretHash = null, testPrivateKey = privateKey)))
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, true))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            assertEquals(userId, awaitItem())
        }
    }

    @Test
    fun `user without recovery secret and non-private user`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val privateKey = mockk<PrivateKey> {
            every { isPrimary } returns true
        }
        val user = mockUser(
            userId,
            listOf(mockUserKey(testRecoverySecretHash = null, testPrivateKey = privateKey)),
            testPrivate = false
        )
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, true))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `user without recovery secret but no primary key`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val privateKey = mockk<PrivateKey> {
            every { isPrimary } returns false
        }
        val user = mockUser(userId, listOf(mockUserKey(testRecoverySecretHash = null, testPrivateKey = privateKey)))
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, true))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `user with existing recovery secret`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val user = mockUser(userId, listOf(mockUserKey(testRecoverySecretHash = "hash")))
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, true))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `user with inactive key`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val user = mockUser(userId, listOf(mockUserKey(testRecoverySecretHash = null, isActive = false)))
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, true))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun `user with disabled device recovery setting`() = runTest {
        // GIVEN
        val userId = UserId("user-1")
        val user = mockUser(userId, listOf(mockUserKey(testRecoverySecretHash = "hash", isActive = false)))
        val userDeviceRecoveryFlow = MutableStateFlow(Pair(user, /* device recovery setting */ false))

        every { observeUserDeviceRecovery() } returns userDeviceRecoveryFlow

        // WHEN
        tested().test {
            // THEN
            expectNoEvents()
        }
    }
}
