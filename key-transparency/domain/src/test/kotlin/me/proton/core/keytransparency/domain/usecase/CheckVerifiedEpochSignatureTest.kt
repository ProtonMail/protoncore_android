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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class CheckVerifiedEpochSignatureTest {

    private lateinit var checkVerifiedEpochSignature: CheckVerifiedEpochSignature
    private val userRepository = mockk<UserRepository>()
    private val cryptoContext = mockk<CryptoContext>()

    @BeforeTest
    fun setUp() {
        checkVerifiedEpochSignature = CheckVerifiedEpochSignature(
            userRepository,
            cryptoContext
        )
    }

    @Test
    fun `valid signature is checked`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val verifiedEpoch = VerifiedEpoch(
            "data",
            "signature"
        )
        val testKey = "key"
        val user = mockk<User> {
            every { keys } returns listOf(
                mockk {
                    every { privateKey.key } returns testKey
                    every { privateKey.isActive } returns true
                    every { privateKey.canVerify } returns true
                    every { privateKey.isPrimary } returns true
                    every { privateKey.canEncrypt } returns true
                }
            )
        }
        coEvery { userRepository.getUser(userId) } returns user
        every { cryptoContext.pgpCrypto.getPublicKey(testKey) } returns testKey
        val expectedContext = VerificationContext(
            value = Constants.KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT,
            required = VerificationContext.ContextRequirement.Required.Always
        )
        every {
            cryptoContext.pgpCrypto.verifyData(
                verifiedEpoch.data.toByteArray(),
                verifiedEpoch.signature,
                testKey,
                VerificationTime.Now,
                verificationContext = expectedContext
            )
        } returns true
        // when
        checkVerifiedEpochSignature(
            userId,
            verifiedEpoch
        )
        // then
        verify {
            cryptoContext.pgpCrypto.verifyData(
                verifiedEpoch.data.toByteArray(),
                verifiedEpoch.signature,
                testKey,
                VerificationTime.Now,
                expectedContext
            )
        }
    }

    @Test
    fun `invalid signature throws exception`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val verifiedEpoch = VerifiedEpoch(
            "data",
            "signature"
        )
        val testKey = "key"
        val user = mockk<User> {
            every { keys } returns listOf(
                mockk {
                    every { privateKey.key } returns testKey
                    every { privateKey.isActive } returns true
                    every { privateKey.canVerify } returns true
                    every { privateKey.isPrimary } returns true
                    every { privateKey.canEncrypt } returns true
                }
            )
        }
        coEvery { userRepository.getUser(userId) } returns user
        every { cryptoContext.pgpCrypto.getPublicKey(testKey) } returns testKey
        val expectedContext = VerificationContext(
            value = Constants.KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT,
            required = VerificationContext.ContextRequirement.Required.Always
        )
        every {
            cryptoContext.pgpCrypto.verifyData(
                verifiedEpoch.data.toByteArray(),
                verifiedEpoch.signature,
                testKey,
                VerificationTime.Now,
                expectedContext
            )
        } returns false
        // when
        assertFailsWith<KeyTransparencyException> {
            checkVerifiedEpochSignature(
                userId,
                verifiedEpoch
            )
        }
        // then
        verify {
            cryptoContext.pgpCrypto.verifyData(
                verifiedEpoch.data.toByteArray(),
                verifiedEpoch.signature,
                testKey,
                VerificationTime.Now,
                expectedContext
            )
        }
    }
}
