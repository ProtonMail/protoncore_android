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
import me.proton.core.user.domain.Constants
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith

class VerifySignedKeyListSignatureTest {
    private lateinit var verifySignedKeyListSignature: VerifySignedKeyListSignature
    private val cryptoContext = mockk<CryptoContext>()

    @BeforeTest
    fun setUp() {
        verifySignedKeyListSignature = VerifySignedKeyListSignature(cryptoContext)
    }

    @Test
    fun `if skl signature is invalid, verification fails`() = runTest {
        // given
        val testKey = "public-key"
        val publicAddress = mockk<PublicAddress> {
            every { keys } returns listOf(
                mockk {
                    every { publicKey.key } returns testKey
                    every { publicKey.canVerify } returns true
                    every { publicKey.isActive } returns true
                }
            )
        }
        val sklData = "data"
        val sklSignature = "signature"
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns sklData
            every { signature } returns sklSignature
        }
        every {
            cryptoContext.pgpCrypto.getVerifiedTimestampOfText(
                sklData,
                sklSignature,
                testKey,
                verificationContext = any()
            )
        } returns null
        // when
        assertFailsWith<KeyTransparencyException> { verifySignedKeyListSignature(publicAddress, skl) }
        // then
        verify {
            cryptoContext.pgpCrypto.getVerifiedTimestampOfText(
                sklData,
                sklSignature,
                testKey,
                verificationContext = match { it.value == Constants.signedKeyListContextValue }
            )
        }
    }
}
