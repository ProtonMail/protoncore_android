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

import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.extension.primary
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyHolderExtensionsTests {

    private val context = TestCryptoContext()
    private val keyHolder1: KeyHolder = TestKeyHolder("user1", context, keyCount = 4, passphraseCount = 1)
    private val keyHolder2: KeyHolder = TestKeyHolder("user2", context, keyCount = 2, passphraseCount = 1)

    private fun ByteArray.allEqual(element: Byte) = all { it == element }

    @Test
    fun canUnlock() {
        // canUnlock = with valid passphrase.

        assertTrue(keyHolder1.keys.primary()?.privateKey?.canUnlock(context)!!)
        assertTrue(keyHolder1.keys.first().privateKey.canUnlock(context))
        assertEquals(1, keyHolder1.keys.count { it.privateKey.canUnlock(context) })

        assertTrue(keyHolder2.keys.primary()?.privateKey?.canUnlock(context)!!)
        assertTrue(keyHolder2.keys.first().privateKey.canUnlock(context))
        assertEquals(1, keyHolder2.keys.count { it.privateKey.canUnlock(context) })
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_String() {
        val message = "message"

        keyHolder1.useKeys(context) {
            val encryptedMessage = encryptText(message)
            val signature = signText(message)

            val decryptedText = decryptText(encryptedMessage)
            assertNotNull(decryptTextOrNull(encryptedMessage))

            assertTrue(verifyText(decryptedText, signature))
            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_ByteArray() {
        val message = "message"
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val encryptedData = encryptData(data)
            val signatureData = signData(data)

            val decryptedData = decryptData(encryptedData)
            assertNotNull(decryptDataOrNull(encryptedData))

            assertTrue(verifyData(decryptedData, signatureData))
            assertTrue(data.contentEquals(decryptedData))
        }
    }

    @Test
    fun useKeys_encryptAndSign_decryptAndVerify_String() {
        val message = "message"

        keyHolder1.useKeys(context) {
            val encryptedAndSignedMessage = encryptAndSignText(message)
            val decryptedSignedText = decryptAndVerifyText(encryptedAndSignedMessage)

            assertNotNull(decryptAndVerifyTextOrNull(encryptedAndSignedMessage))
            assertEquals(message, decryptedSignedText)
        }
    }

    @Test
    fun useKeys_encryptAndSign_decryptAndVerify_ByteArray() {
        val message = "message"
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val encryptedAndSignedData = encryptAndSignData(data)
            val decryptedSignedData = decryptAndVerifyData(encryptedAndSignedData)

            assertNotNull(decryptAndVerifyDataOrNull(encryptedAndSignedData))
            assertTrue(data.contentEquals(decryptedSignedData))
        }
    }

    @Test
    fun useKeys_encrypt_sign__close__decrypt_verify() {
        val message = "message"

        keyHolder1.useKeys(context) {
            val encryptedMessage = encryptText(message)
            val signature = signText(message)

            encryptedMessage to signature
        }.also { (encryptedMessage, signature) ->

            keyHolder1.useKeys(context) {
                val decryptedText = decryptText(encryptedMessage)

                assertNotNull(decryptTextOrNull(encryptedMessage))
                assertTrue(verifyText(decryptedText, signature))
                assertEquals(message, decryptedText)
            }
        }
    }

    @Test
    fun useKeys_useWrongKeyHolder() {
        val message = "message"

        keyHolder1.useKeys(context) {
            encryptText(message)
        }.also {
            keyHolder2.useKeys(context) {
                assertNull(decryptTextOrNull(it))
                assertFailsWith(CryptoException::class) { decryptText(it) }
            }
        }
    }

    @Test
    fun useKeys_mustClearUnlockedKeys() {
        lateinit var keyHolderContext: KeyHolderContext

        keyHolder1.useKeys(context) {
            keyHolderContext = this

            assertTrue(privateKeyRing.keys.any())
            assertTrue(privateKeyRing.unlockedKeys.any())
            assertNotNull(privateKeyRing.unlockedPrimaryKey)

            assertTrue(publicKeyRing.keys.any())
            assertNotNull(publicKeyRing.primaryKey)
        }

        // Verify no more unlocked keys bits.
        assertTrue(keyHolderContext.privateKeyRing.unlockedPrimaryKey.unlockedKey.value.allEqual(0))
        assertTrue(keyHolderContext.privateKeyRing.unlockedKeys.all { key -> key.unlockedKey.value.allEqual(0) })
    }

    @Test
    fun without_useKeys_encrypt__decrypt_with_useKeys() {
        val message = "message"

        val encrypted = keyHolder1.keys.primary()?.privateKey?.publicKey(context)?.encryptText(context, message)
        assertNotNull(encrypted)

        keyHolder1.useKeys(context) {
            val decryptedText = decryptText(encrypted)

            assertNotNull(decryptTextOrNull(encrypted))
            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun without_useKeys_encrypt_decrypt() {
        val message = "message"

        val publicKey = keyHolder1.keys.primary()?.privateKey?.publicKey(context)
        assertNotNull(publicKey)

        val encrypted = publicKey.encryptText(context, message)

        val unlockedPrivateKey = keyHolder1.keys.primary()?.privateKey?.unlock(context)
        assertNotNull(unlockedPrivateKey)

        unlockedPrivateKey.use {
            val decryptedText = it.decryptText(context, encrypted)

            assertNotNull(it.decryptTextOrNull(context, encrypted))
            assertEquals(message, decryptedText)

            val signature = it.signText(context, message)
            assertTrue(publicKey.verifyText(context, decryptedText, signature))
        }

        // Verify no more unlocked keys bits.
        assertTrue(unlockedPrivateKey.unlockedKey.value.allEqual(0))
    }
}
