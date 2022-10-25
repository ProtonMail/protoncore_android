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

import android.util.Log
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.crypto.common.pgp.split
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.extension.publicKeyRing
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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

    private fun getTempFile(filename: String) = File.createTempFile("$filename.", "")

    private fun ByteArray.getFile(filename: String): File = getTempFile(filename).apply { appendBytes(this@getFile) }

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
    fun useKeys_keyPacket_encrypt_decrypt_ByteArray() {
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()

            val encryptedData = encryptData(data, keyPacket)
            val decryptedData = decryptData(encryptedData, keyPacket)

            assertTrue(data.contentEquals(decryptedData))
        }
    }

    @Test
    fun useKeys_sessionKey_encrypt_decrypt_ByteArray() {
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val sessionKey = generateNewSessionKey()

            val encryptedData = encryptData(data, sessionKey)
            val decryptedData = decryptData(encryptedData, sessionKey)

            assertTrue(data.contentEquals(decryptedData))
        }
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_LargeByteArray() {
        val data = Random.nextBytes(10 * 1000 * 1000) // 10MB

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
    fun useKeys_encrypt_sign_decrypt_verify_File() {
        val data = message.toByteArray()
        val file = data.getFile("file")

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            val encryptedFile = encryptFile(file, getTempFile("encrypted"), keyPacket)
            val signatureFile = signFile(file)

            val decryptedFile = decryptFile(encryptedFile, getTempFile("decrypted"), keyPacket)
            val decryptedOrNullFile = decryptFileOrNull(encryptedFile, getTempFile("decryptedOrNull"), keyPacket)
            assertNotNull(decryptedOrNullFile)

            assertTrue(verifyFile(decryptedFile, signatureFile))
            assertTrue(file.readBytes().contentEquals(decryptedFile.file.readBytes()))

            encryptedFile.delete()
            decryptedFile.file.delete()
            decryptedOrNullFile.file.delete()
        }
        file.delete()
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_File_SessionKey() {
        val file = getTempFile("chunk")
        file.appendBytes(Random.nextBytes(4 * 1000 * 1000)) // 4MB

        keyHolder1.useKeys(context) {
            generateNewSessionKey().use { sessionKey ->
                val encryptedFile = encryptFile(file, getTempFile("encrypted"), sessionKey)
                val signatureFile = signFile(file)
                val decryptedFile = decryptFile(encryptedFile, getTempFile("decrypted"), sessionKey)

                assertTrue(verifyFile(decryptedFile, signatureFile))
                assertTrue(file.readBytes().contentEquals(decryptedFile.file.readBytes()))

                encryptedFile.delete()
                decryptedFile.file.delete()
            }
        }
        file.delete()
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_File_KeyPacket() {
        val file = getTempFile("chunk")
        file.appendBytes(Random.nextBytes(4 * 1000 * 1000)) // 4MB

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            val encryptedFile = encryptFile(file, getTempFile("encrypted"), keyPacket)
            val signatureFile = signFile(file)

            val decryptedFile = decryptFile(encryptedFile, getTempFile("decrypted"), keyPacket)
            val decryptedOrNullFile = decryptFileOrNull(encryptedFile, getTempFile("decryptedOrNull"), keyPacket)
            assertNotNull(decryptedOrNullFile)

            assertTrue(verifyFile(decryptedFile, signatureFile))
            assertTrue(file.readBytes().contentEquals(decryptedFile.file.readBytes()))
            assertTrue(file.readBytes().contentEquals(decryptedOrNullFile.file.readBytes()))

            encryptedFile.delete()
            decryptedFile.file.delete()
            decryptedOrNullFile.file.delete()
        }
        file.delete()
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_File_KeyPacket_vs_sessionKey() {
        val file = getTempFile("chunk")
        file.appendBytes(Random.nextBytes(4 * 1000 * 1000)) // 4MB

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            decryptSessionKey(keyPacket).use { sessionKey ->
                val encryptedFile = encryptFile(file, getTempFile("encrypted"), keyPacket)
                val signatureFile = signFile(file)

                val decryptedFile1 = decryptFile(encryptedFile, getTempFile("decrypted"), keyPacket)
                val decryptedFile2 = decryptFile(encryptedFile, getTempFile("decrypted"), sessionKey)
                val decryptedOrNullFile = decryptFileOrNull(encryptedFile, getTempFile("decryptedOrNull"), keyPacket)
                assertNotNull(decryptedOrNullFile)

                assertTrue(verifyFile(decryptedFile1, signatureFile))
                assertTrue(file.readBytes().contentEquals(decryptedFile1.file.readBytes()))
                assertTrue(decryptedFile1.file.readBytes().contentEquals(decryptedFile2.file.readBytes()))
                assertTrue(decryptedFile2.file.readBytes().contentEquals(decryptedOrNullFile.file.readBytes()))

                encryptedFile.delete()
                decryptedFile1.file.delete()
                decryptedFile2.file.delete()
                decryptedOrNullFile.file.delete()
            }
        }
        file.delete()
    }

    @Test
    fun useKeys_encrypt_sign_decrypt_verify_LargeFile() {
        val file = getTempFile("large")
        file.appendBytes(Random.nextBytes(10 * 1000 * 1000)) // 10MB

        keyHolder1.useKeys(context) {
            val keyPacket: KeyPacket
            val encryptedFile: EncryptedFile
            val signatureFile: Signature
            val decryptedFile: DecryptedFile

            val keyPacketTimeMillis = measureTimeMillis {
                keyPacket = generateNewKeyPacket()
            }

            val encryptTimeMillis = measureTimeMillis {
                encryptedFile = encryptFile(file, getTempFile("encrypted"), keyPacket)
            }

            assertNotNull(encryptedFile)

            val signTimeMillis = measureTimeMillis {
                signatureFile = signFile(file)
            }

            assertNotNull(encryptedFile)

            val decryptTimeMillis = measureTimeMillis {
                decryptedFile = decryptFile(encryptedFile, getTempFile("decrypted"), keyPacket)
            }

            assertNotNull(decryptedFile)

            val decryptedOrNullFile = decryptFileOrNull(encryptedFile, getTempFile("decryptedOrNull"), keyPacket)
            assertNotNull(decryptedOrNullFile)

            val verifyTimeMillis = measureTimeMillis {
                assertTrue(verifyFile(decryptedFile, signatureFile))
            }
            assertTrue(file.readBytes().contentEquals(decryptedFile.file.readBytes()))

            Log.d("Crypto Stream", "File size: ${Files.size(file.toPath())}")
            Log.d("Crypto Stream", "encryptTimeMillis: $encryptTimeMillis")
            Log.d("Crypto Stream", "signTimeMillis: $signTimeMillis")
            Log.d("Crypto Stream", "decryptTimeMillis: $decryptTimeMillis")
            Log.d("Crypto Stream", "verifyTimeMillis: $verifyTimeMillis")

            encryptedFile.delete()
            decryptedFile.file.delete()
            decryptedOrNullFile.file.delete()
        }
        file.delete()
    }

    @Test
    fun useKeys_encrypt_decrypt_SessionKey_from_encryptData() {
        val data = message.toByteArray()

        keyHolder1.useKeys(context) {
            val sessionKey = decryptSessionKey(encryptData(data).split(context.pgpCrypto).keyPacket())

            val encryptedKey = encryptSessionKey(sessionKey)
            val decryptedKey = decryptSessionKey(encryptedKey)

            assertEquals(sessionKey, decryptedKey)
        }
    }

    @Test
    fun useKeys_encrypt_decrypt_SessionKey_from_encryptFile() {
        val data = message.toByteArray()
        val file = data.getFile("filename")

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            val encryptedFile = encryptFile(file, getTempFile("decrypted"), keyPacket)
            val sessionKey = decryptSessionKey(keyPacket)

            val encryptedKey = encryptSessionKey(sessionKey)
            val decryptedKey = decryptSessionKey(encryptedKey)

            assertEquals(sessionKey, decryptedKey)

            encryptedFile.delete()
        }
        file.delete()
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
    fun useKeys_encryptAndSign_decryptAndVerify_File() {
        val file = getTempFile("large")
        file.appendBytes(Random.nextBytes(10 * 1000 * 1000)) // 10MB

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            val encryptedFile = encryptAndSignFile(file, getTempFile("encrypted"), keyPacket)
            val decryptedFile = decryptAndVerifyFile(encryptedFile, getTempFile("decrypted"), keyPacket)
            val decryptedOrNullFile =
                decryptAndVerifyFileOrNull(encryptedFile, getTempFile("decryptedOrNull"), keyPacket)

            assertNotNull(decryptedOrNullFile)
            assertEquals(VerificationStatus.Success, decryptedFile.status)
            assertTrue(file.readBytes().contentEquals(decryptedFile.file.readBytes()))

            encryptedFile.delete()
            decryptedFile.file.delete()
            decryptedOrNullFile.file.delete()
        }
        file.delete()
    }

    @Test
    fun useKeys_encryptAndSign_decryptAndVerify_fail_File() {
        val file = getTempFile("large")
        file.appendBytes(Random.nextBytes(10 * 1000 * 1000)) // 10MB

        keyHolder1.useKeys(context) {
            val keyPacket = generateNewKeyPacket()
            keyPacket to encryptAndSignFile(file, getTempFile("encrypted"), keyPacket)
        }.also { (keyPacket, encryptedFile) ->
            keyHolder2.useKeys(context) {
                val tempFile = getTempFile("decrypted")
                assertNull(decryptAndVerifyFileOrNull(encryptedFile, tempFile, keyPacket))
                tempFile.delete()
            }
            encryptedFile.delete()
        }
        file.delete()
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
    fun useKeys_sign__close__get_timestamp() {
        keyHolder1.useKeys(context) {
            val signedText = signText(message)
            message to signedText
        }.also { (message, signature) ->
            keyHolder1.useKeys(context) {
                val timestamp = getVerifiedTimestampOfText(message, signature)
                assertNotNull(timestamp)
            }
        }
    }

    @Test
    fun useKeys_encrypt_sign_data__close__decrypt_verify() {
        keyHolder1.useKeys(context) {
            val signature = signData(message.toByteArray())
            message to signature
        }.also { (message, signature) ->
            keyHolder1.useKeys(context) {
                val timestamp = getVerifiedTimestampOfData(message.toByteArray(), signature)
                assertNotNull(timestamp)
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

        val encrypted2 = PublicKey(
            key = TestKeys.privateKey1_PublicKey,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        ).encryptText(context, message)

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

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_String() {
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signTextEncrypted(message, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyTextEncrypted(message, encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertTrue(verified)
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_String_corrupted() {
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signTextEncrypted(message, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyTextEncrypted(message + "corrupted", encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertFalse(verified)
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_String_wrong_key() {
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signTextEncrypted(message, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and wrongly verifies it with its own public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyTextEncrypted(message + "corrupted", encryptedSignature, keyHolder2.publicKeyRing(context))
        }
        assertFalse(verified)
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_ByteArray() {
        val data = message.toByteArray()
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signDataEncrypted(data, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyDataEncrypted(data, encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertTrue(verified)
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_ByteArray_corrupted() {
        val data = message.toByteArray()
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signDataEncrypted(data, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyDataEncrypted(data + "corrupted".toByteArray(), encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertFalse(verified)
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_File() {
        val data = message.toByteArray()
        val file = data.getFile("file")
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signFileEncrypted(file, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyFileEncrypted(file, encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertTrue(verified)
        file.delete()
    }

    @Test
    fun useKeys_generate_and_verify_encrypted_signature_for_File_corrupted() {
        val data = message.toByteArray() + "corrupted".toByteArray()
        val file = data.getFile("file")
        // Key holder 1 signs the message and encrypts the signature for Key Holder 2
        val encryptedSignature = keyHolder1.useKeys(context) {
            signFileEncrypted(file, keyHolder2.publicKeyRing(context))
        }
        // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
        val verified = keyHolder2.useKeys(context) {
            verifyFileEncrypted(file, encryptedSignature, keyHolder1.publicKeyRing(context))
        }
        assertTrue(verified)
        file.delete()
    }

    @Test
    fun useKeys_generate_encrypt_and_decrypt_nested_key() {
        // given
        val encryptedNestedKey = keyHolder1.useKeys(context){
            val nestedKey = generateNestedPrivateKey("user", "example.proton.me")
            encryptAndSignNestedKey(nestedKey)
        }
        // when
        val decryptedNestedKey = keyHolder1.useKeys(context){
            decryptAndVerifyNestedKeyOrThrow(encryptedNestedKey)
        }
        // then
        assertEquals(
            encryptedNestedKey.privateKey.fingerprint(context),
            decryptedNestedKey.privateKey.fingerprint(context)
        )
        assertTrue(decryptedNestedKey.privateKey.isActive)
    }

    @Test
    fun useKeys_generate_encrypt_and_decrypt_nested_key_wrong_signature() {
        // given
        val encryptedNestedKey = keyHolder1.useKeysAs(context){ encryptContext ->
            val nestedKey = encryptContext.generateNestedPrivateKey("user", "example.proton.me")
            keyHolder2.useKeysAs(context){ signingContext ->
                signingContext.encryptAndSignNestedKey(
                    nestedKey,
                    encryptKeyRing = encryptContext.publicKeyRing
                )
            }

        }
        // when & then
        assertFails {
            keyHolder1.useKeys(context){
                decryptAndVerifyNestedKeyOrThrow(encryptedNestedKey)
            }
        }
    }
}
