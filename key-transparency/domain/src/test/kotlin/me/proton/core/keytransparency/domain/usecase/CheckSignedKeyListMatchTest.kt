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

package me.proton.core.keytransparency.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.util.kotlin.toBoolean
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith

class CheckSignedKeyListMatchTest {

    private lateinit var checkSignedKeyListMatch: CheckSignedKeyListMatch
    private val cryptoContext = mockk<CryptoContext>()

    @BeforeTest
    fun setUp() {
        checkSignedKeyListMatch = CheckSignedKeyListMatch(
            cryptoContext
        )
    }

    @Test
    fun `if skl matches the public keys, verification succeeds`() = runTest {
        // given
        val testKey = "public-key"
        val testFlags = 3
        val primary = 1
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                    every { publicKey.isPrimary } returns primary.toBoolean()
                    every { flags } returns testFlags
                }
            )
        }
        val fingerprint = "fingerprint"
        every { cryptoContext.pgpCrypto.getFingerprint(testKey) } returns fingerprint
        val jsonSHA256Fingerprints = """["fingerprint1","fingerprint2"]"""
        every { cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey) } returns jsonSHA256Fingerprints
        val sklData = """
            |[{
            |"Fingerprint":$fingerprint,
            |"Flags":$testFlags,
            |"SHA256Fingerprints":$jsonSHA256Fingerprints,
            |"Primary":$primary
            |}]
        """.trimMargin()
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        // when
        checkSignedKeyListMatch(publicAddress, skl)
        // then
        verify {
            cryptoContext.pgpCrypto.getFingerprint(testKey)
            cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey)
        }
    }

    @Test
    fun `if the fingerprint doesn't match, verification fails`() = runTest {
        // given
        val testKey = "public-key"
        val testFlags = 3
        val primary = 1
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                    every { publicKey.isPrimary } returns primary.toBoolean()
                    every { flags } returns testFlags
                }
            )
        }
        val fingerprint = "fingerprint"
        every { cryptoContext.pgpCrypto.getFingerprint(testKey) } returns "other-fingerprint"
        val jsonSHA256Fingerprints = """["fingerprint1","fingerprint2"]"""
        every { cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey) } returns jsonSHA256Fingerprints
        val sklData = """
            |[{
            |"Fingerprint":$fingerprint,
            |"Flags":$testFlags,
            |"SHA256Fingerprints":$jsonSHA256Fingerprints,
            |"Primary":$primary
            |}]
        """.trimMargin()
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        // when
        assertFailsWith<KeyTransparencyException> { checkSignedKeyListMatch(publicAddress, skl) }
    }

    @Test
    fun `if the primariness doesn't match, verification fails`() = runTest {
        // given
        val testKey = "public-key"
        val testFlags = 3
        val primary = 1
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                    every { publicKey.isPrimary } returns primary.toBoolean().not()
                    every { flags } returns testFlags
                }
            )
        }
        val fingerprint = "fingerprint"
        every { cryptoContext.pgpCrypto.getFingerprint(testKey) } returns fingerprint
        val jsonSHA256Fingerprints = """["fingerprint1","fingerprint2"]"""
        every { cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey) } returns jsonSHA256Fingerprints
        val sklData = """
            |[{
            |"Fingerprint":$fingerprint,
            |"Flags":$testFlags,
            |"SHA256Fingerprints":$jsonSHA256Fingerprints,
            |"Primary":$primary
            |}]
        """.trimMargin()
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        // when
        assertFailsWith<KeyTransparencyException> { checkSignedKeyListMatch(publicAddress, skl) }
    }

    @Test
    fun `if the flags don't match, verification fails`() = runTest {
        // given
        val testKey = "public-key"
        val testFlags = 3
        val primary = 1
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                    every { publicKey.isPrimary } returns primary.toBoolean()
                    every { flags } returns 0
                }
            )
        }
        val fingerprint = "fingerprint"
        every { cryptoContext.pgpCrypto.getFingerprint(testKey) } returns fingerprint
        val jsonSHA256Fingerprints = """["fingerprint1","fingerprint2"]"""
        every { cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey) } returns jsonSHA256Fingerprints
        val sklData = """
            |[{
            |"Fingerprint":$fingerprint,
            |"Flags":$testFlags,
            |"SHA256Fingerprints":$jsonSHA256Fingerprints,
            |"Primary":$primary
            |}]
        """.trimMargin()
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        // when
        assertFailsWith<KeyTransparencyException> { checkSignedKeyListMatch(publicAddress, skl) }
    }

    @Test
    fun `if the sha256 fingerprint don't match, verification fails`() = runTest {
        // given
        val testKey = "public-key"
        val testFlags = 3
        val primary = 1
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                    every { publicKey.isPrimary } returns primary.toBoolean()
                    every { flags } returns testFlags
                }
            )
        }
        val fingerprint = "fingerprint"
        every { cryptoContext.pgpCrypto.getFingerprint(testKey) } returns fingerprint
        val jsonSHA256Fingerprints = """["fingerprint1","fingerprint2"]"""
        every {
            cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(testKey)
        } returns """["fingerprint1","fingerprint2", "other-fingerprint3"]"""
        val sklData = """
            |[{
            |"Fingerprint":$fingerprint,
            |"Flags":$testFlags,
            |"SHA256Fingerprints":$jsonSHA256Fingerprints,
            |"Primary":$primary
            |}]
        """.trimMargin()
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        // when
        assertFailsWith<KeyTransparencyException> { checkSignedKeyListMatch(publicAddress, skl) }
    }
}
