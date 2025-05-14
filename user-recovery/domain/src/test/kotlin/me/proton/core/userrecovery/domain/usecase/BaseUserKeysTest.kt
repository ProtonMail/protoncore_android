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

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRemoteDataSource
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.HashUtils
import org.junit.Before

/**
 * Test data: 1 User -> 2 UserKey -> 1 primary/active and 1 inactive.
 */
abstract class BaseUserKeysTest {

    internal val testSecretValid = "valid"
    internal val testSecretValidHash = HashUtils.sha256(testSecretValid)
    internal val testSecretValidSignature = "signature-valid"
    internal val testSecretInvalid = "invalid"
    internal val testSecretInvalidHash = HashUtils.sha256(testSecretInvalid)

    internal val testUnlockedKey = mockk<UnlockedKey> {
        every { this@mockk.value } returns "unlocked".toByteArray()
        every { this@mockk.close() } just Runs
    }

    internal val testUnlockedKeyPrivateKey = mockk<UnlockedPrivateKey> {
        every { this@mockk.unlockedKey } returns testUnlockedKey
        every { this@mockk.close() } just Runs
    }

    internal val testPrivateKeyInactive = mockk<PrivateKey> {
        every { this@mockk.isActive } returns false
        every { this@mockk.isPrimary } returns false
        every { this@mockk.key } returns "inactive.key"
    }
    internal val testPrivateKeyPrimary = mockk<PrivateKey> {
        every { this@mockk.isActive } returns true
        every { this@mockk.isPrimary } returns true
        every { this@mockk.key } returns "active.key"
        every { this@mockk.canEncrypt } returns true
        every { this@mockk.canVerify } returns true
    }
    internal val testKey1 = mockk<UserKey>(relaxed = true) {
        every { this@mockk.privateKey } returns testPrivateKeyPrimary
        every { this@mockk.recoverySecret } returns testSecretValid
        every { this@mockk.recoverySecretHash } returns testSecretValidHash
        every { this@mockk.recoverySecretSignature } returns testSecretValidSignature
        every { this@mockk.active } returns true
        every { this@mockk.keyId } returns KeyId("testKey1")
    }
    internal val testKey2 = mockk<UserKey>(relaxed = true) {
        every { this@mockk.privateKey } returns testPrivateKeyInactive
        every { this@mockk.recoverySecret } returns testSecretInvalid
        every { this@mockk.recoverySecretHash } returns testSecretInvalidHash
        every { this@mockk.active } returns false
        every { this@mockk.keyId } returns KeyId("testKey2")
    }
    internal val testUser = mockk<User> {
        every { this@mockk.userId } returns UserId("userId")
        every { this@mockk.keys } returns listOf(testKey1, testKey2)
    }

    internal val testUserManager = mockk<UserManager> {
        coEvery { this@mockk.getUser(any(), any()) } returns testUser
    }

    internal val testUserRemoteDataSource = mockk<UserRemoteDataSource> {
        coEvery { this@mockk.fetch(any()) } returns testUser
    }

    internal val testUserRepository = mockk<UserRepository> {
        coJustRun { this@mockk.updateUser(any()) }
    }

    internal val testDecodedSecret1 = "decodedSecret1".toByteArray()
    internal val testDecodedSecret2 = "decodedSecret2".toByteArray()

    internal val testPgpCrypto = mockk<PGPCrypto>(relaxed = true) {
        every { this@mockk.getBase64Decoded(testSecretValid) } returns testDecodedSecret1
        every { this@mockk.getBase64Decoded(testSecretInvalid) } returns testDecodedSecret2
        every { this@mockk.deserializeKeys(any()) } returns listOf(testUnlockedKey.value)
    }
    internal val testCryptoContext = mockk<CryptoContext>(relaxed = true) {
        every { this@mockk.pgpCrypto } returns testPgpCrypto
    }
    internal val testEncryptedPassphrase = EncryptedByteArray("passphrase".toByteArray())
    internal val testPassphraseRepository = mockk<PassphraseRepository> {
        coEvery { this@mockk.getPassphrase(any()) } returns testEncryptedPassphrase
    }

    @Before
    open fun before() {
        mockkStatic(PrivateKey::unlockOrNull)
        every { testPrivateKeyPrimary.unlockOrNull(any()) } returns testUnlockedKeyPrivateKey
        every { testPrivateKeyInactive.unlockOrNull(any()) } returns testUnlockedKeyPrivateKey
    }
}
