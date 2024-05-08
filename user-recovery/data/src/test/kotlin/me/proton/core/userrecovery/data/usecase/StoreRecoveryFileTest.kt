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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.userrecovery.data.mock.TEST_RECOVERY_SECRET
import me.proton.core.userrecovery.data.mock.TEST_RECOVERY_SECRET_HASH
import me.proton.core.userrecovery.data.mock.mockUser
import me.proton.core.userrecovery.data.mock.mockUserKey
import me.proton.core.userrecovery.domain.entity.RecoveryFile
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.util.android.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StoreRecoveryFileTest {
    @MockK
    private lateinit var clock: Clock

    @MockK
    private lateinit var deviceRecoveryRepository: DeviceRecoveryRepository

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: StoreRecoveryFile

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { clock.currentEpochMillis() } returns 100
        tested = StoreRecoveryFile(deviceRecoveryRepository, clock, userManager)
    }

    @Test
    fun `no primary key`() = runTest {
        // GIVEN
        val user = mockk<User> {
            every { keys } returns emptyList()
        }
        coEvery { userManager.getUser(any()) } returns user

        // THEN
        val throwable = assertFailsWith<IllegalArgumentException> {
            // WHEN
            tested("recoveryFile", mockk())
        }
        assertEquals("Primary key is missing.", throwable.message)
    }

    @Test
    fun `no recovery secret`() = runTest {
        // GIVEN
        val userKey = mockUserKey(testRecoverySecretHash = null)
        every { userKey.privateKey } returns mockk {
            every { isPrimary } returns true
        }

        val user = mockUser(UserId("test-user"), listOf(userKey))
        coEvery { userManager.getUser(any()) } returns user
        coJustRun { deviceRecoveryRepository.insertRecoveryFile(any()) }

        // THEN
        val throwable = assertFailsWith<IllegalArgumentException> {
            // WHEN
            tested("recoveryFile", mockk())
        }
        assertEquals("Recovery secret is missing.", throwable.message)
    }

    @Test
    fun `store recovery file`() = runTest {
        // GIVEN
        val userId = UserId("test-user")
        val userKey = mockUserKey(testRecoverySecretHash = TEST_RECOVERY_SECRET_HASH)
        every { userKey.privateKey } returns mockk {
            every { isPrimary } returns true
        }

        val user = mockUser(userId, listOf(userKey))
        coEvery { userManager.getUser(any()) } returns user
        coJustRun { deviceRecoveryRepository.insertRecoveryFile(any()) }

        // WHEN
        tested("recoveryFile", userId)

        // THEN
        val recoveryFileSlot = slot<RecoveryFile>()
        coVerify { deviceRecoveryRepository.insertRecoveryFile(capture(recoveryFileSlot)) }
        assertEquals(
            RecoveryFile(
                userId = userId,
                createdAtUtcMillis = 100,
                recoveryFile = "recoveryFile",
                recoverySecretHash = TEST_RECOVERY_SECRET_HASH
            ), recoveryFileSlot.captured
        )
    }
}
