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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.userrecovery.domain.entity.RecoveryFile
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.domain.usecase.GetRecoveryInactiveUserKeys
import me.proton.core.userrecovery.domain.usecase.GetRecoveryPrivateKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecoverInactivePrivateKeysTest {
    @MockK
    private lateinit var getRecoveryInactiveUserKeys: GetRecoveryInactiveUserKeys

    @MockK
    private lateinit var getRecoveryPrivateKeys: GetRecoveryPrivateKeys

    @MockK
    private lateinit var deviceRecoveryRepository: DeviceRecoveryRepository

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: RecoverInactivePrivateKeys

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = RecoverInactivePrivateKeys(
            getRecoveryInactiveUserKeys,
            getRecoveryPrivateKeys,
            deviceRecoveryRepository,
            userManager
        )
    }

    @Test
    fun `recover private keys`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        val testKeyId = KeyId("key-id")
        val testPrivateKey = PrivateKey(
            key = "key",
            isPrimary = true,
            passphrase = null
        )
        val userKeyMock = mockk<UserKey> {
            every { userId } returns testUserId
            every { keyId } returns testKeyId
            every { privateKey } returns testPrivateKey
        }

        coEvery { deviceRecoveryRepository.getRecoveryFiles(testUserId) } returns listOf(
            RecoveryFile(testUserId, 100, "recoveryFile", "hash")
        )
        coEvery { getRecoveryPrivateKeys(testUserId, any()) } returns listOf(testPrivateKey)
        coEvery { getRecoveryInactiveUserKeys(testUserId, any()) } returns listOf(userKeyMock)
        coEvery { userManager.reactivateKey(any()) } returns mockk()

        // WHEN
        tested(testUserId)

        // THEN
        coVerify { userManager.reactivateKey(userKeyMock) }
    }
}
