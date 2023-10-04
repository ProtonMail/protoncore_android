/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.key.domain

import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PublicAddressKeyCryptoTest {

    private val unlockedKey: UnlockedKey = object : UnlockedKey {
        override val value = "Unarmored".toByteArray()
        override fun close() = Unit
    }

    private val publicKey1 = PublicKey(
        key = "publicKey1",
        isPrimary = true,
        isActive = true,
        canEncrypt = true,
        canVerify = true
    )

    private val publicKey2 = PublicKey(
        key = "publicKey2",
        isPrimary = true,
        isActive = true,
        canEncrypt = false,
        canVerify = false
    )

    private val publicAddressKey1 = PublicAddressKey(
        email = "email@example.com",
        flags = 3, // not compromised, not obsolete.
        publicKey = publicKey1
    )

    private val publicAddressKey2 = PublicAddressKey(
        email = "email@example.com",
        flags = 0, // compromised, obsolete.
        publicKey = publicKey2
    )

    private val publicAddress1 = PublicAddress(
        email = "email@example.com",
        recipientType = 1,
        mimeType = null,
        keys = listOf(publicAddressKey1),
        signedKeyList = null,
        ignoreKT = 0
    )

    private val publicAddress2 = PublicAddress(
        email = "email@example.com",
        recipientType = 1,
        mimeType = null,
        keys = listOf(publicAddressKey2),
        signedKeyList = null,
        ignoreKT = 0
    )

    private val context = TestCryptoContext().also {
        it.unlockedKeys[publicKey1.key] = unlockedKey.value
        it.unlockedKeys[publicKey2.key] = unlockedKey.value
    }

    private val message = "message"

    @Test
    fun publicAddress_encryptText() {
        val encryptedText = publicAddress1.encryptText(context, message)
        assertNotNull(encryptedText)
    }

    @Test(expected = CryptoException::class)
    fun publicKey_encryptText_canEncrypt_false() {
        publicAddress2.encryptText(context, message)
    }

    @Test
    fun publicKey_verifyText() {
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val verified = publicAddress1.verifyText(context, message, signature)
        assertTrue(verified)
    }

    @Test
    fun publicKey_verifyText_canVerify_false() {
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val verified = publicAddress2.verifyText(context, message, signature)
        assertFalse(verified)
    }

    @Test
    fun publicKey_verifyData_default() {
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val verified = publicAddress1.verifyData(context, "message".toByteArray(), signature)
        assertTrue(verified)
    }

    @Test
    fun publicAddress_encryptData() {
        val testData = "message".toByteArray()
        val result = publicAddress1.encryptData(context, testData)
        assertNotNull(result)
    }

    @Test
    fun publicAddress_encryptSessionKey() {
        val sessionKey = SessionKey("key".toByteArray())
        val result = publicAddress1.encryptSessionKey(context, sessionKey)
        assertNotNull(result)
    }

    @Test
    fun publicAddress_getVerifiedTimestampOfText() {
        val testData = "message"
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val result = publicAddress1.getVerifiedTimestampOfText(context, testData, signature)
        assertNotNull(result)
    }

    @Test
    fun publicAddress_getVerifiedTimestampOfData() {
        val testData = "message".toByteArray()
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val result = publicAddress1.getVerifiedTimestampOfData(context, testData, signature)
        assertNotNull(result)
    }
}
