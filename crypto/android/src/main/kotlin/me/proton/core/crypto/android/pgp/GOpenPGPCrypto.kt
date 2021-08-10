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

package me.proton.core.crypto.android.pgp

import com.google.crypto.tink.subtle.Base64
import com.proton.gopenpgp.constants.Constants
import com.proton.gopenpgp.crypto.Crypto
import com.proton.gopenpgp.crypto.Key
import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.PGPMessage
import com.proton.gopenpgp.crypto.PGPSignature
import com.proton.gopenpgp.crypto.PGPSplitMessage
import com.proton.gopenpgp.crypto.PlainMessage
import com.proton.gopenpgp.crypto.PlainMessageMetadata
import com.proton.gopenpgp.crypto.SessionKey
import com.proton.gopenpgp.helper.ExplicitVerifyMessage
import com.proton.gopenpgp.helper.Go2AndroidReader
import com.proton.gopenpgp.helper.Helper
import com.proton.gopenpgp.helper.Mobile2GoReader
import com.proton.gopenpgp.helper.Mobile2GoWriter
import com.proton.gopenpgp.srp.Srp
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException
import java.io.Closeable
import java.io.File
import java.security.SecureRandom

/**
 * [PGPCrypto] implementation based on GOpenPGP Android library.
 */
@Suppress("TooManyFunctions")
class GOpenPGPCrypto : PGPCrypto {

    // region Private

    private class CloseableUnlockedKey(val value: Key) : Closeable {
        override fun close() {
            value.clearPrivateParams()
        }
    }

    private class CloseableUnlockedKeyRing(val value: KeyRing) : Closeable {
        override fun close() {
            value.clearPrivateParams()
        }
    }

    private fun <R> List<CloseableUnlockedKey>.use(block: (List<CloseableUnlockedKey>) -> R): R {
        try {
            return block(this)
        } finally {
            forEach { it.close() }
        }
    }

    private fun newKey(key: Key) = CloseableUnlockedKey(key)
    private fun newKey(key: Unarmored) = newKey(Crypto.newKey(key))
    private fun newKeys(keys: List<Unarmored>) = keys.map { newKey(it) }

    private fun newKeyRing(key: CloseableUnlockedKey) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(key.value))

    private fun newKeyRing(keys: List<CloseableUnlockedKey>) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(null).apply { keys.forEach { addKey(it.value) } })

    private fun newKeyRing(key: Armored) =
        Crypto.newKeyRing(Crypto.newKeyFromArmored(key))

    private fun newKeyRing(keys: List<Armored>) =
        Crypto.newKeyRing(null).apply { keys.map { Crypto.newKeyFromArmored(it) }.forEach { addKey(it) } }

    private fun encrypt(
        plainMessage: PlainMessage,
        publicKey: Armored
    ): EncryptedMessage {
        val publicKeyRing = newKeyRing(publicKey)
        return publicKeyRing.encrypt(plainMessage, null).armored
    }

    private fun encryptAndSign(
        plainMessage: PlainMessage,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        val publicKeyRing = newKeyRing(publicKey)
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return publicKeyRing.encrypt(plainMessage, keyRing.value).armored
            }
        }
    }

    private fun encrypt(
        source: File,
        destination: File,
        publicKey: Armored
    ): EncryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val writer = Mobile2GoWriter(fileOutputStream.writer())
                val plainMessageMetadata = PlainMessageMetadata(true, source.name, source.lastModified() / 1000)
                val result = newKeyRing(publicKey).encryptSplitStream(writer, plainMessageMetadata, null).use {
                    fileInputStream.reader().copyTo(it)
                }
                return EncryptedFile(
                    file = destination,
                    keyPacket = result.keyPacket
                )
            }
        }
    }

    private fun encryptAndSign(
        source: File,
        destination: File,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val writer = Mobile2GoWriter(fileOutputStream.writer())
                val plainMessageMetadata = PlainMessageMetadata(true, source.name, source.lastModified() / 1000)
                val publicKeyRing = newKeyRing(publicKey)
                newKey(unlockedKey).use { key ->
                    newKeyRing(key).use { keyRing ->
                        val result = publicKeyRing.encryptSplitStream(writer, plainMessageMetadata, keyRing.value).use {
                            fileInputStream.reader().copyTo(it)
                        }
                        return EncryptedFile(
                            file = destination,
                            keyPacket = result.keyPacket
                        )
                    }
                }
            }
        }
    }

    private fun decrypt(
        source: File,
        destination: File,
        unlockedKey: Unarmored,
        keyPacket: KeyPacket
    ): DecryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val reader = Mobile2GoReader(fileInputStream.mobileReader())
                newKey(unlockedKey).use { key ->
                    newKeyRing(key).use { keyRing ->
                        val plainMessageReader = keyRing.value.decryptSplitStream(keyPacket, reader, null, 0)
                        Go2AndroidReader(plainMessageReader).copyTo(fileOutputStream.writer())
                        return DecryptedFile(
                            file = destination,
                            status = VerificationStatus.Unknown,
                            filename = plainMessageReader.metadata.filename,
                            lastModifiedEpochSeconds = plainMessageReader.metadata.modTime
                        )
                    }
                }
            }
        }
    }

    private fun decryptAndVerify(
        source: File,
        destination: File,
        keyPacket: KeyPacket,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long
    ): DecryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val reader = Mobile2GoReader(fileInputStream.mobileReader())
                val publicKeyRing = newKeyRing(publicKeys)
                return newKeys(unlockedKeys).use { keys ->
                    newKeyRing(keys).use { keyRing ->
                        val plainMessageReader =
                            keyRing.value.decryptSplitStream(keyPacket, reader, publicKeyRing, validAtUtc)
                        Go2AndroidReader(plainMessageReader).copyTo(fileOutputStream.writer())
                        DecryptedFile(
                            file = destination,
                            status = Helper.verifySignatureExplicit(plainMessageReader).toVerificationStatus(),
                            filename = plainMessageReader.metadata.filename,
                            lastModifiedEpochSeconds = plainMessageReader.metadata.modTime
                        )
                    }
                }
            }
        }
    }

    private inline fun <T> decrypt(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        return decrypt(pgpMessage, unlockedKey, block)
    }

    private inline fun <T> decrypt(
        pgpMessage: PGPMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return block(keyRing.value.decrypt(pgpMessage, null, 0))
            }
        }
    }

    private inline fun <T> decryptAndVerify(
        msg: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
        crossinline block: (ExplicitVerifyMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(msg)
        val publicKeyRing = newKeyRing(publicKeys)
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { keyRing ->
                block(Helper.decryptExplicitVerify(pgpMessage, keyRing.value, publicKeyRing, validAtUtc))
            }
        }
    }

    private fun sign(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored
    ): Signature {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return keyRing.value.signDetached(plainMessage).armored
            }
        }
    }

    private fun sign(
        source: File,
        unlockedKey: Unarmored
    ): Signature {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            newKey(unlockedKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    return keyRing.value.signDetachedStream(reader).armored
                }
            }
        }
    }

    private fun verify(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = newKeyRing(publicKey)
        publicKeyRing.verifyDetached(plainMessage, pgpSignature, validAtUtc)
    }.isSuccess

    private fun verify(
        source: File,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = runCatching {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            val pgpSignature = PGPSignature(signature)
            val publicKeyRing = newKeyRing(publicKey)
            publicKeyRing.verifyDetachedStream(reader, pgpSignature, validAtUtc)
        }
    }.isSuccess

    // endregion

    // region Lock/Unlock

    override fun lock(
        unlockedKey: Unarmored,
        passphrase: ByteArray
    ): Armored {
        newKey(unlockedKey).use { key ->
            return key.value.lock(passphrase).armor()
        }
    }

    override fun unlock(
        privateKey: Armored,
        passphrase: ByteArray
    ): UnlockedKey = runCatching {
        val key = Crypto.newKeyFromArmored(privateKey)
        val unlockedKey = key.unlock(passphrase)
        return GOpenPGPUnlockedKey(unlockedKey)
    }.getOrElse { throw CryptoException("PrivateKey cannot be unlocked using passphrase.", it) }

    // endregion

    // region Encrypt

    override fun encryptText(
        plainText: String,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encrypt(PlainMessage(plainText), publicKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted.", it) }

    override fun encryptData(
        data: ByteArray,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encrypt(PlainMessage(data), publicKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    override fun encryptFile(
        source: File,
        destination: File,
        publicKey: Armored
    ): EncryptedFile = runCatching {
        encrypt(source, destination, publicKey)
    }.getOrElse { throw CryptoException("File cannot be encrypted.", it) }

    override fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSign(PlainMessage(plainText), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSign(PlainMessage(data), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignFile(
        source: File,
        destination: File,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedFile = runCatching {
        encryptAndSign(source, destination, publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("File cannot be encrypted or signed.", it) }

    override fun encryptSessionKey(
        keyPacket: ByteArray,
        publicKey: Armored
    ): ByteArray = runCatching {
        val publicKeyRing = newKeyRing(publicKey)
        val sessionKey = SessionKey(keyPacket, Constants.AES256)
        return publicKeyRing.encryptSessionKey(sessionKey)
    }.getOrElse { throw CryptoException("KeyPacket cannot be encrypted.", it) }

    override fun encryptSessionKey(
        keyPacket: ByteArray,
        password: ByteArray
    ): ByteArray = runCatching {
        val sessionKey = SessionKey(keyPacket, Constants.AES256)
        return Crypto.encryptSessionKeyWithPassword(sessionKey, password)
    }.getOrElse { throw CryptoException("KeyPacket cannot be encrypted with password.", it) }

    // endregion

    // region Decrypt

    override fun decryptText(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): String = runCatching {
        decrypt(message, unlockedKey) { it.string }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptData(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        decrypt(message, unlockedKey) { it.binary }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptFile(
        source: EncryptedFile,
        destination: File,
        unlockedKey: Unarmored
    ): DecryptedFile = runCatching {
        decrypt(source.file, destination, unlockedKey, source.keyPacket)
    }.getOrElse { throw CryptoException("File cannot be decrypted.", it) }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
    ): DecryptedText = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys, validAtUtc) {
            DecryptedText(
                it.message.string,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
    ): DecryptedData = runCatching {
        decryptAndVerify(message, publicKeys, unlockedKeys, validAtUtc) {
            DecryptedData(
                it.message.binary,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyFile(
        source: EncryptedFile,
        destination: File,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long
    ): DecryptedFile = runCatching {
        decryptAndVerify(source.file, destination, source.keyPacket, publicKeys, unlockedKeys, validAtUtc)
    }.getOrElse { throw CryptoException("File cannot be decrypted.", it) }

    override fun decryptSessionKey(
        keyPacket: ByteArray,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                keyRing.value.decryptSessionKey(keyPacket).key
            }
        }
    }.getOrElse { throw CryptoException("KeyPacket cannot be decrypted.", it) }

    // endregion

    // region Sign

    override fun signText(
        plainText: String,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(PlainMessage(plainText), unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }

    override fun signData(
        data: ByteArray,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(PlainMessage(data), unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    override fun signFile(
        file: File,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        sign(file, unlockedKey)
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    // endregion

    // region Verify

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = verify(PlainMessage(plainText), signature, publicKey, validAtUtc)

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = verify(PlainMessage(data), signature, publicKey, validAtUtc)

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = verify(file.file, signature, publicKey, validAtUtc)

    // endregion

    // region update

    override fun updatePrivateKeyPassphrase(privateKey: String, oldPassphrase: ByteArray, newPassphrase: ByteArray): Armored? =
        runCatching {
            check(oldPassphrase.isNotEmpty()) { "The passphrase for generating key can't be empty." }
            check(newPassphrase.isNotEmpty()) { "The passphrase for generating key can't be empty." }

            Helper.updatePrivateKeyPassphrase(privateKey, oldPassphrase, newPassphrase)
        }.getOrThrow()

    // endregion

    // region Get

    override fun getArmored(
        data: Unarmored
    ): Armored = runCatching {
        Crypto.newPGPMessage(data).armored
    }.getOrElse { throw CryptoException("Armored cannot be extracted from Unarmored.", it) }

    override fun getUnarmored(
        data: Armored
    ): Unarmored = runCatching {
        Crypto.newPGPMessageFromArmored(data).binary
    }.getOrElse { throw CryptoException("Unarmored cannot be extracted from Armored.", it) }

    override fun getEncryptedPackets(
        message: EncryptedMessage
    ): List<EncryptedPacket> = runCatching {
        val pgpSplitMessage = PGPSplitMessage(message)
        return listOf(
            EncryptedPacket(pgpSplitMessage.keyPacket, PacketType.Key),
            EncryptedPacket(pgpSplitMessage.dataPacket, PacketType.Data)
        )
    }.getOrElse { throw CryptoException("EncryptedFile cannot be extracted from EncryptedMessage.", it) }

    override fun getPublicKey(
        privateKey: Armored
    ): Armored = runCatching {
        Crypto.newKeyFromArmored(privateKey).armoredPublicKey
    }.getOrElse { throw CryptoException("Public key cannot be extracted from privateKey.", it) }

    override fun getFingerprint(
        key: Armored
    ): String = runCatching {
        Crypto.newKeyFromArmored(key).fingerprint
    }.getOrElse { throw CryptoException("Fingerprint cannot be extracted from key.", it) }

    override fun getJsonSHA256Fingerprints(
        key: Armored
    ): String = runCatching {
        Helper.getJsonSHA256Fingerprints(key).toString(Charsets.UTF_8)
    }.getOrElse { throw CryptoException("SHA256 Fingerprints cannot be extracted from key.", it) }

    override fun getPassphrase(
        password: ByteArray,
        encodedSalt: String
    ): ByteArray {
        val passphraseHashSize = 31
        val decodedKeySalt: ByteArray = Base64.decode(encodedSalt, Base64.DEFAULT)
        return Srp.mailboxPassword(password, decodedKeySalt).use {
            it.array.copyOfRange(it.array.size - passphraseHashSize, it.array.size)
        }
    }

    override fun generateNewKeySalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val keySalt = Base64.encodeToString(salt, Base64.DEFAULT)
        // Truncate newline character.
        return keySalt.substring(0, keySalt.length - 1)
    }

    override fun generateNewToken(size: Long): ByteArray {
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        val secret = Crypto.randomToken(size)
        val token = secret.toHexString().toByteArray(Charsets.UTF_8)
        require(token.size == secret.size * 2)
        return token
    }

    @Suppress("MagicNumber")
    override fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray
    ): Armored = runCatching {
        check(passphrase.isNotEmpty()) { "The passphrase for generating key can't be empty." }

        // Set offset 24h in the past.
        Crypto.setKeyGenerationOffset(-86_400L)

        val email = "$username@$domain"
        Helper.generateKey(email, email, passphrase, PGPCrypto.KeyType.X25519.toString(), 0)
    }.getOrElse { throw CryptoException("Key cannot be generated.", it) }

    override fun updateTime(epochSeconds: Long) {
        Crypto.updateTime(epochSeconds)
    }

    // endregion

    companion object {
        // 32K is usually not far from the optimal buffer size on Android devices.
        const val DEFAULT_BUFFER_SIZE = 32768
    }
}
