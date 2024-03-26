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
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.key.domain.unlockOrNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class GetRecoveryFileTest : BaseUserKeysTest() {

    private lateinit var tested: GetRecoveryFile

    @Before
    override fun before() {
        super.before()
        tested = GetRecoveryFile(
            userManager = testUserManager,
            cryptoContext = testCryptoContext
        )
    }

    @Test
    fun getRecoverFileHappyPath() = runTest {
        // WHEN
        tested.invoke(testUser.userId)

        // THEN
        verify(exactly = 1) { testPrivateKeyPrimary.unlockOrNull(any()) }
        verify(exactly = 0) { testPrivateKeyInactive.unlockOrNull(any()) }
        verify(exactly = 1) { testPgpCrypto.getBase64Decoded(testSecretValid) }
        verify(exactly = 0) { testPgpCrypto.getBase64Decoded(testSecretInvalid) }
        verify(exactly = 1) { testPgpCrypto.encryptDataWithPassword(any(), testDecodedSecret1) }
        verify(exactly = 0) { testPgpCrypto.encryptDataWithPassword(any(), testDecodedSecret2) }
    }

    @Test
    fun getRecoverFileThrowIllegalArgumentWhenNoPrimary() = runTest {
        // GIVEN
        every { testPrivateKeyPrimary.isPrimary } returns false

        // WHEN
        assertFailsWith<IllegalArgumentException> {
            tested.invoke(testUser.userId)
        }
    }

    @Test
    fun getRecoverFileThrowIllegalArgumentWhenNoSecret() = runTest {
        // GIVEN
        every { testKey1.recoverySecret } returns null
        every { testKey2.recoverySecret } returns null

        // WHEN
        assertFailsWith<IllegalArgumentException> {
            tested.invoke(testUser.userId)
        }
    }

    @Test
    fun getRecoverFileThrowIllegalStateWhenNoActive() = runTest {
        // GIVEN
        every { testKey1.active } returns false
        every { testKey2.active } returns false

        // WHEN
        assertFailsWith<IllegalStateException> {
            tested.invoke(testUser.userId)
        }
    }
}
