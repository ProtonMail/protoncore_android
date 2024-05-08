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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.pgp.exception.CryptoException
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetRecoveryPrivateKeysTest : BaseUserKeysTest() {

    private lateinit var tested: GetRecoveryPrivateKeys

    @Before
    override fun before() {
        super.before()
        tested = GetRecoveryPrivateKeys(
            userRemoteDataSource = testUserRemoteDataSource,
            passphraseRepository = testPassphraseRepository,
            cryptoContext = testCryptoContext
        )
    }

    @Test
    fun getRecoveryPrivateKeysHappyPath() = runTest {
        // WHEN
        val result = tested.invoke(testUser.userId, "encryptedMessage")

        // THEN
        assertTrue(result.size == 1)
        verify(exactly = 1) { testPgpCrypto.getBase64Decoded(testSecretValid) }
        verify(exactly = 0) { testPgpCrypto.getBase64Decoded(testSecretInvalid) }

        verify(exactly = 1) { testPgpCrypto.decryptDataWithPassword(any(), testDecodedSecret1) }
        verify(exactly = 0) { testPgpCrypto.decryptDataWithPassword(any(), testDecodedSecret2) }
    }

    @Test
    fun getRecoveryPrivateKeysGetSecretFromRemote() = runTest {
        // WHEN
        tested.invoke(testUser.userId, "encryptedMessage")

        // THEN
        coVerify { testUserRemoteDataSource.fetch(any()) }
    }

    @Test
    fun getRecoveryPrivateKeysReturnsEmptyListWhenDecryptFail() = runTest {
        // GIVEN
        every { testPgpCrypto.decryptDataWithPassword(any(), any()) } throws CryptoException()

        // WHEN
        val result = tested.invoke(testUser.userId, "encryptedMessage")

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun getRecoveryPrivateKeysThrowIllegalStateWhenNoPassphrase() = runTest {
        // GIVEN
        coEvery { testPassphraseRepository.getPassphrase(any()) } returns null

        // WHEN
        assertFailsWith<IllegalStateException> {
            tested.invoke(testUser.userId, "encryptedMessage")
        }
    }
}
