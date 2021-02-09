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

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.extension.primary
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyHolderTest {

    private val context = AndroidCryptoContext()
    private val keyHolder1: KeyHolder = TestKeyHolder(context, TestKeys.privateKey1, TestKeys.privateKey1_Passphrase)
    private val keyHolder2: KeyHolder = TestKeyHolder(context, TestKeys.privateKey2, TestKeys.privateKey2_Passphrase)

    private val message =
        """
        Lorèm ipsum dolor sit ämet, conséctétür adipiscing elit. Vivamus eget enim a sem volutpat posuere eget eu leo.
        Sed sollicitudin felis massa, sit amet iaculis justo semper eu.
        Vivamus suscipit nulla eu orci euismod, ut mattis lorem luctus.
        Etiam tincidunt non lorem quis sollicitudin. Praesent auctor lacus sed dictum consectetur.
        Ut sagittis, tortor at maximus efficitur, enim odio rhoncus nisi, eget semper odio odio et purus.
        Nulla nec cursus libero, eu mollis nibh.
        Morbi arcu arcu, mattis vitae tristique porttitor, rhoncus nec tortor. Quisque nec sodales enim, volutpat mollis dui.
        Mauris sit amet interdum mi, in faucibus ex. Quisque volutpat risus mi, eu lacinia odio tempus ac. Nulla facilisi.
        Fusce fermentum ut turpis at vehicula. Pellentesque ultricies est quis hendrerit convallis. Morbi quis nisi lorem.
        """.trimIndent()

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
        keyHolder1.useKeys(context) {
            val encryptedText = encryptText(message)
            val signedText = signText(message)

            val decryptedText = decryptText(encryptedText)
            assertNotNull(decryptTextOrNull(encryptedText))

            assertTrue(verifyText(decryptedText, signedText))
            assertEquals(message, decryptedText)
        }
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_ByteArray() {
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val encryptedData = encryptData(data)
            val signedData = signData(data)

            val decryptedData = decryptData(encryptedData)
            assertNotNull(decryptDataOrNull(encryptedData))

            assertTrue(verifyData(decryptedData, signedData))
            assertTrue(data.contentEquals(decryptedData))
        }
    }

    @Test
    fun useKeys_encryptAndSign_decryptAndVerify_String() {
        keyHolder1.useKeys(context) {
            val encryptedSignedText = encryptAndSignText(message)
            val decryptedSignedText = decryptAndVerifyText(encryptedSignedText)

            assertNotNull(decryptAndVerifyTextOrNull(encryptedSignedText))
            assertEquals(VerificationStatus.Success, decryptedSignedText.status)
            assertEquals(message, decryptedSignedText.text)
        }
    }

    @Test
    fun useKeys_encryptAndSign_decryptAndVerify_ByteArray() {
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val encryptedSignedData = encryptAndSignData(data)
            val decryptedSignedData = decryptAndVerifyData(encryptedSignedData)

            assertNotNull(decryptAndVerifyDataOrNull(encryptedSignedData))
            assertEquals(VerificationStatus.Success, decryptedSignedData.status)
            assertTrue(data.contentEquals(decryptedSignedData.data))
        }
    }

    @Test
    fun useKeys_encrypt_sign__close__decrypt_verify() {
        keyHolder1.useKeys(context) {
            val encryptedText = encryptText(message)
            val signedText = signText(message)

            encryptedText to signedText
        }.also { (encryptedMessage, signature) ->

            keyHolder1.useKeys(context) {
                val decryptedMessage = decryptText(encryptedMessage)

                assertTrue(verifyText(decryptedMessage, signature))
                assertEquals(message, decryptedMessage)
            }
        }
    }

    @Test
    fun useKeys_useWrongKeyHolder() {
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
            assertNotNull(privateKeyRing.unlockedPrimaryKey.unlockedKey.value)
            privateKeyRing.unlockedKeys.forEach { assertNotNull(it.unlockedKey.value) }

            assertTrue(publicKeyRing.keys.any())
            assertNotNull(publicKeyRing.primaryKey)
        }

        // Verify no more unlocked keys bits.
        assertTrue(keyHolderContext.privateKeyRing.unlockedPrimaryKey.unlockedKey.value.allEqual(0) ?: false)
        assertTrue(keyHolderContext.privateKeyRing.unlockedKeys.all { key -> key.unlockedKey.value.allEqual(0) })
    }

    @Test
    fun without_useKeys_encrypt__decrypt_with_useKeys() {
        val encrypted1 = keyHolder1.keys.primary()?.privateKey?.publicKey(context)?.encryptText(context, message)
        val signature1 = keyHolder1.keys.primary()?.privateKey?.unlock(context)?.signText(context, message)
        assertNotNull(encrypted1)
        assertNotNull(signature1)

        val encrypted2 = PublicKey(TestKeys.privateKey1_PublicKey, true).encryptText(context, message)

        keyHolder1.useKeys(context) {
            val decrypted1 = decryptText(encrypted1)
            assertNotNull(decryptTextOrNull(encrypted1))
            assertTrue(verifyText(decrypted1, signature1))
            assertEquals(message, decrypted1)

            val decrypted2 = decryptText(encrypted2)
            assertNotNull(decryptTextOrNull(encrypted2))
            assertEquals(message, decrypted2)
        }

        keyHolder2.useKeys(context) {
            assertNull(decryptTextOrNull(encrypted1))
            assertNull(decryptTextOrNull(encrypted2))

            assertFailsWith(CryptoException::class) { decryptText(encrypted1) }
            assertFailsWith(CryptoException::class) { decryptText(encrypted2) }
        }
    }

    @Test
    fun without_useKeys_encrypt_decrypt() {
        val publicKey = keyHolder1.keys.primary()?.privateKey?.publicKey(context)
        assertNotNull(publicKey)

        val encrypted = publicKey.encryptText(context, message)

        val unlockedPrivateKey = keyHolder1.keys.primary()?.privateKey?.unlock(context)
        assertNotNull(unlockedPrivateKey)

        unlockedPrivateKey.use {
            val decrypted = it.decryptText(context, encrypted)
            assertNotNull(it.decryptTextOrNull(context, encrypted))
            assertEquals(message, decrypted)

            val signature = it.signText(context, message)
            assertTrue(publicKey.verifyText(context, decrypted, signature))
        }

        // Verify no more unlocked keys bits.
        assertTrue(unlockedPrivateKey.unlockedKey.value.allEqual(0))
    }
}
