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

package me.proton.core.key.domain.extension

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.pgp.updatePrivateKeyPassphraseOrNull
import me.proton.core.key.domain.TestCryptoContext
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class UpdatePrivateKeyKtTest {

    private val context = TestCryptoContext()

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.key.domain.PrivateKeyCryptoKt")
        mockkStatic("me.proton.core.crypto.common.pgp.PGPCryptoOrNullKt")
        mockkStatic("me.proton.core.crypto.common.keystore.EncryptedByteArrayKt")
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.key.domain.PrivateKeyCryptoKt")
        unmockkStatic("me.proton.core.crypto.common.pgp.PGPCryptoOrNullKt")
        unmockkStatic("me.proton.core.crypto.common.keystore.EncryptedByteArrayKt")
    }

    @Test
    fun `PrivateKey_updateIsActive returns false`() {
        val passphrase = EncryptedByteArray("test-passphrase".toByteArray())
        val privateKey = spyk(
            PrivateKey(
                "test-key",
                isPrimary = true,
                isActive = true,
                canEncrypt = true,
                canVerify = true,
                passphrase = passphrase
            )
        )
        every { privateKey.canUnlock(context, passphrase) } returns false
        val resultPrivateKey = privateKey.updateIsActive(context, passphrase)
        assertNotNull(resultPrivateKey)
        assertFalse(resultPrivateKey.isActive)
        verify { privateKey.canUnlock(context, passphrase) }
    }

    @Test
    fun `PrivateKey_updateIsActive returns true`() {
        val passphrase = EncryptedByteArray("test-passphrase".toByteArray())
        val privateKey = spyk(
            PrivateKey(
                "test-key",
                isPrimary = true,
                isActive = true,
                canEncrypt = true,
                canVerify = true,
                passphrase = passphrase
            )
        )
        every { privateKey.canUnlock(context, passphrase) } returns true
        val resultPrivateKey = privateKey.updateIsActive(context, passphrase)
        assertNotNull(resultPrivateKey)
        assertTrue(resultPrivateKey.isActive)
        verify { privateKey.canUnlock(context, passphrase) }
    }

    @Test
    fun `Armored_updatePrivateKeyPassphraseOrNull returns non null`() {
        val passphrase = "test-passphrase".toByteArray()
        val newPassphrase = "test-new-passphrase".toByteArray()
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val testContext = spyk(context)
        val armored = "test-armored"
        every { testContext.pgpCrypto } returns pgpCrypto
        every { pgpCrypto.updatePrivateKeyPassphraseOrNull(any(), any(), any()) } returns "new-armored"
        val resultArmored = armored.updatePrivateKeyPassphraseOrNull(testContext, passphrase, newPassphrase)
        assertNotNull(resultArmored)
        assertEquals("new-armored", resultArmored)
        verify { pgpCrypto.updatePrivateKeyPassphraseOrNull(armored, passphrase, newPassphrase) }
    }

    @Test
    fun `Armored_updatePrivateKeyPassphraseOrNull returns null`() {
        val passphrase = "test-passphrase".toByteArray()
        val newPassphrase = "test-new-passphrase".toByteArray()
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val testContext = spyk(context)
        val armored = "test-armored"
        every { testContext.pgpCrypto } returns pgpCrypto
        every { pgpCrypto.updatePrivateKeyPassphraseOrNull(any(), any(), any()) } returns null
        val resultArmored = armored.updatePrivateKeyPassphraseOrNull(testContext, passphrase, newPassphrase)
        assertNull(resultArmored)
        verify { pgpCrypto.updatePrivateKeyPassphraseOrNull(armored, passphrase, newPassphrase) }
    }

    @Test
    fun `KeyHolderPrivateKey_updatePrivateKeyPassphraseOrNull`() {
        val keyHolderPrivateKey = mockk<KeyHolderPrivateKey>(relaxed = true)
        val testContext = spyk(context)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val encryptedPassphrase = spyk(EncryptedByteArray("passphrase".toByteArray()))
        val passphrase = PlainByteArray("passphrase".toByteArray())
        val privateKey = PrivateKey(
            key = "key-id",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = encryptedPassphrase
        )
        val newPassphrase = "test-new-passphrase".toByteArray()

        every { keyHolderPrivateKey.keyId } returns KeyId("key-id")
        every { testContext.pgpCrypto } returns pgpCrypto
        every { keyHolderPrivateKey.privateKey } returns privateKey
        every { encryptedPassphrase.decrypt(any()) } returns passphrase
        every { pgpCrypto.updatePrivateKeyPassphraseOrNull(any(), any(), any()) } returns "private-key"
        // WHEN
        val resultKey = keyHolderPrivateKey.updatePrivateKeyPassphraseOrNull(testContext, newPassphrase)
        // THEN
        assertNotNull(resultKey)
        assertEquals(Key(KeyId("key-id"), "private-key"), resultKey)
    }
}