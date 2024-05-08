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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetRecoveryFileTest : BaseUserKeysTest() {
    @MockK
    private lateinit var getExistingVerifiedRecoverySecret: GetExistingVerifiedRecoverySecret

    @MockK
    private lateinit var getUnlockedUserKeys: GetUnlockedUserKeys

    private lateinit var tested: GetRecoveryFile

    @Before
    override fun before() {
        super.before()
        MockKAnnotations.init(this)
        tested = GetRecoveryFile(
            cryptoContext = testCryptoContext,
            getExistingVerifiedRecoverySecret = getExistingVerifiedRecoverySecret,
            getUnlockedUserKeys = getUnlockedUserKeys
        )
    }

    @Test
    fun getRecoverFileHappyPath() = runTest {
        // GIVEN
        coEvery { getExistingVerifiedRecoverySecret(any()) } returns testSecretValid
        coEvery { getUnlockedUserKeys(any()) } returns listOf(testUnlockedKey)

        // WHEN
        val result = tested.invoke(testUser.userId)

        // THEN
        assertEquals(1, result.keyCount)

        verify(exactly = 1) { testPgpCrypto.getBase64Decoded(testSecretValid) }
        verify(exactly = 0) { testPgpCrypto.getBase64Decoded(testSecretInvalid) }
        verify(exactly = 1) { testPgpCrypto.encryptDataWithPassword(any(), testDecodedSecret1) }
        verify(exactly = 0) { testPgpCrypto.encryptDataWithPassword(any(), testDecodedSecret2) }
        verify(exactly = 1) { testUnlockedKey.close() }
    }

    @Test
    fun getRecoverFileThrowIllegalArgumentWhenNoSecret() = runTest {
        // GIVEN
        coEvery { getExistingVerifiedRecoverySecret(any()) } returns null

        // WHEN
        assertFailsWith<IllegalArgumentException> {
            tested.invoke(testUser.userId)
        }
    }

    @Test
    fun getRecoverFileThrowIllegalStateWhenNoActive() = runTest {
        // GIVEN
        coEvery { getExistingVerifiedRecoverySecret(any()) } returns testSecretValid
        coEvery { getUnlockedUserKeys(any()) } returns listOf()

        // WHEN
        assertFailsWith<IllegalStateException> {
            tested.invoke(testUser.userId)
        }
    }
}
