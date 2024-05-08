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

package me.proton.core.userrecovery.domain.usecase

import io.mockk.every
import kotlinx.coroutines.test.runTest
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.KeyId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class GetRecoveryInactiveUserKeysTest : BaseUserKeysTest() {

    private lateinit var tested: GetRecoveryInactiveUserKeys

    @Before
    override fun before() {
        super.before()
        tested = GetRecoveryInactiveUserKeys(
            userManager = testUserManager,
            cryptoContext = testCryptoContext
        )
    }

    @Test
    fun getRecoveryInactivePrivateKeysHappyPath() = runTest {
        // GIVEN
        val recoverable = listOf(testPrivateKeyPrimary, testPrivateKeyInactive)

        // WHEN
        val result = tested.invoke(testUser.userId, recoverable)

        // THEN
        assertTrue(result.size == 1)
    }

    @Test
    fun getRecoveryInactivePrivateKeysCannotUnlock() = runTest {
        // GIVEN
        every { testPrivateKeyPrimary.canUnlock(any()) } returns false
        every { testPrivateKeyInactive.canUnlock(any()) } returns false
        val recoverable = listOf(testPrivateKeyPrimary, testPrivateKeyInactive)

        // WHEN
        val result = tested.invoke(testUser.userId, recoverable)

        // THEN
        assertTrue(result.isEmpty())
    }
}
