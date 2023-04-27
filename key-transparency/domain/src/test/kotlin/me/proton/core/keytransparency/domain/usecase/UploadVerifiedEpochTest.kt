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
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData.Companion.toJson
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class UploadVerifiedEpochTest {
    private lateinit var uploadVerifiedEpoch: UploadVerifiedEpoch
    private val cryptoContext: CryptoContext = mockk()
    private val userRepository: UserRepository = mockk()
    private val keyTransparencyRepository: KeyTransparencyRepository = mockk()

    @BeforeTest
    fun setUp() {
        uploadVerifiedEpoch = UploadVerifiedEpoch(
            cryptoContext,
            userRepository,
            keyTransparencyRepository
        )
    }

    @Test
    fun `sign the epoch and upload it`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val epochID = 10
        val testKey = "key"
        val testAddressId = AddressId("address-id")
        val passphrase = "passphrase".toByteArray()
        val decryptedPassphrase = PlainByteArray(passphrase)
        val encryptedPassphrase = EncryptedByteArray(passphrase)
        val user = mockk<User> {
            every { keys } returns listOf(
                mockk {
                    every { privateKey.key } returns testKey
                    every { privateKey.isActive } returns true
                    every { privateKey.canVerify } returns true
                    every { privateKey.isPrimary } returns true
                    every { privateKey.canEncrypt } returns true
                    every { privateKey.passphrase } returns encryptedPassphrase
                }
            )
        }
        coEvery { userRepository.getUser(userId) } returns user
        every { cryptoContext.keyStoreCrypto.decrypt(encryptedPassphrase) } returns decryptedPassphrase
        val revision = 10
        val inputVerifiedEpoch = VerifiedEpochData(
            epochID,
            revision,
            100
        )
        val serialized = inputVerifiedEpoch.toJson()
        val unlocked = "unlocked".toByteArray()
        every { cryptoContext.pgpCrypto.unlock(testKey, passphrase) } returns mockk(relaxed = true) {
            every { value } returns unlocked
        }
        every { cryptoContext.pgpCrypto.getPublicKey(testKey) } returns "public-key"
        val signature = "signature"
        val expectedContext = SignatureContext(
            value = Constants.KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT,
            isCritical = true
        )
        every {
            cryptoContext.pgpCrypto.signData(
                serialized.toByteArray(),
                unlocked,
                signatureContext = expectedContext
            )
        } returns signature
        val expectedSignedVerifiedEpoch = VerifiedEpoch(
            serialized,
            signature
        )
        coJustRun { keyTransparencyRepository.uploadVerifiedEpoch(userId, testAddressId, expectedSignedVerifiedEpoch) }
        // when
        uploadVerifiedEpoch(userId, testAddressId, inputVerifiedEpoch)
        // then
        coVerify {
            cryptoContext.pgpCrypto.signData(serialized.toByteArray(), unlocked, signatureContext = expectedContext)
            keyTransparencyRepository.uploadVerifiedEpoch(userId, testAddressId, expectedSignedVerifiedEpoch)
        }
    }
}
