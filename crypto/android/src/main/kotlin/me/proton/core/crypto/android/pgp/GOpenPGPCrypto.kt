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

import androidx.core.util.lruCache
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
import com.proton.gopenpgp.helper.ExplicitVerifyMessage
import com.proton.gopenpgp.helper.Go2AndroidReader
import com.proton.gopenpgp.helper.Helper
import com.proton.gopenpgp.helper.Mobile2GoReader
import com.proton.gopenpgp.helper.Mobile2GoWriter
import com.proton.gopenpgp.srp.Srp
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.pgp.HashKey
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.pgp.unlockOrNull
import java.io.Closeable
import java.io.File
import java.security.SecureRandom

import com.proton.gopenpgp.crypto.SessionKey as InternalSessionKey

/**
 * [PGPCrypto] implementation based on GOpenPGP Android library.
 */
@Suppress("LargeClass", "TooManyFunctions", "MagicNumber", "ClassOrdering")
class GOpenPGPCrypto : PGPCrypto {

    // region Private Closable

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

    // endregion

    // region Private Key

    private fun SessionKey.toInternalSessionKey() = InternalSessionKey(key, Constants.AES256)

    private fun newKey(key: Key) = CloseableUnlockedKey(key)
    private fun newKey(key: Unarmored) = newKey(Crypto.newKey(key))
    private fun newKeys(keys: List<Unarmored>) = keys.map { newKey(it) }

    private fun newKeyRing(key: CloseableUnlockedKey) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(key.value))

    private fun newKeyRing(keys: List<CloseableUnlockedKey>) =
        CloseableUnlockedKeyRing(Crypto.newKeyRing(null).apply { keys.forEach { addKey(it.value) } })

    // endregion

    // region Public Key

    private val cachedKeys = lruCache<Armored, Key>(
        maxSize = KEY_CACHE_LRU_MAX_SIZE,
        create = { armored -> Crypto.newKeyFromArmored(armored) }
    )
    private fun Armored.key() = if (KEY_CACHE_ENABLED) cachedKeys.get(this) else Crypto.newKeyFromArmored(this)
    private fun Armored.keyRing() = Crypto.newKeyRing(key())
    private fun List<Armored>.keyRing() = Crypto.newKeyRing(null).apply { forEach { addKey(it.key()) } }

    // endregion

    // region Private VerificationTime

    private fun VerificationTime.toUtcSeconds(): Long = when (this) {
        is VerificationTime.Ignore -> 0
        is VerificationTime.Now -> Crypto.getUnixTime() // Value updated by updateTime.
        is VerificationTime.Utc -> seconds
    }

    // endregion

    // region Private Encrypt

    private fun encryptMessage(
        plainMessage: PlainMessage,
        publicKey: Armored,
        signKeyRing: KeyRing? = null
    ): EncryptedMessage {
        val publicKeyRing = publicKey.keyRing()
        return publicKeyRing.encrypt(plainMessage, signKeyRing).armored
    }

    private fun encryptMessageWithCompression(
        plainMessage: PlainMessage,
        publicKey: Armored,
        signKeyRing: KeyRing? = null
    ): EncryptedMessage {
        val publicKeyRing = publicKey.keyRing()
        return publicKeyRing.encryptWithCompression(plainMessage, signKeyRing).armored
    }

    private fun encryptMessageSessionKey(
        plainMessage: PlainMessage,
        sessionKey: SessionKey,
        signKeyRing: KeyRing? = null,
    ): DataPacket {
        return sessionKey.toInternalSessionKey().let { internalSessionKey ->
            if (signKeyRing != null) {
                internalSessionKey.encryptAndSign(plainMessage, signKeyRing)
            } else {
                internalSessionKey.encrypt(plainMessage)
            }
        }
    }

    private fun encryptFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        signKeyRing: KeyRing? = null
    ): EncryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val writer = Mobile2GoWriter(fileOutputStream.writer())
                val plainMessageMetadata = PlainMessageMetadata(true, source.name, source.lastModified() / 1000)
                val internalSessionKey = sessionKey.toInternalSessionKey()
                val writeCloser = internalSessionKey.encryptStream(writer, plainMessageMetadata, signKeyRing)
                fileInputStream.reader().copyTo(writeCloser)
                writeCloser.close()
                return destination
            }
        }
    }

    private fun encryptAndSignFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): EncryptedFile {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return encryptFileSessionKey(source, destination, sessionKey, keyRing.value)
            }
        }
    }

    private fun encryptAndSignMessage(
        plainMessage: PlainMessage,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return encryptMessage(plainMessage, publicKey, keyRing.value)
            }
        }
    }

    private fun encryptAndSignMessageWithCompression(
        plainMessage: PlainMessage,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return encryptMessageWithCompression(plainMessage, publicKey, keyRing.value)
            }
        }
    }


    private fun encryptAndSignMessageSessionKey(
        plainMessage: PlainMessage,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
    ): DataPacket {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return encryptMessageSessionKey(plainMessage, sessionKey, keyRing.value)
            }
        }
    }

    // endregion

    // region Private Decrypt

    private inline fun <T> decryptMessage(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        block: (PlainMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        return decryptMessage(pgpMessage, unlockedKey, block)
    }

    private inline fun <T> decryptMessage(
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

    private fun decryptDataSessionKey(
        data: DataPacket,
        sessionKey: SessionKey,
    ): PlainMessage {
        val internalSessionKey = sessionKey.toInternalSessionKey()
        return internalSessionKey.decrypt(data)
    }

    private fun decryptFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        verifyKeyRing: KeyRing? = null,
        validAtUtc: Long = 0
    ): DecryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val reader = Mobile2GoReader(fileInputStream.mobileReader())
                val internalSessionKey = sessionKey.toInternalSessionKey()
                val plainMessageReader = internalSessionKey.decryptStream(reader, verifyKeyRing, validAtUtc)
                Go2AndroidReader(plainMessageReader).copyTo(fileOutputStream.writer())
                return DecryptedFile(
                    file = destination,
                    status = verifyKeyRing?.let {
                        Helper.verifySignatureExplicit(plainMessageReader).toVerificationStatus()
                    } ?: VerificationStatus.Unknown,
                    filename = plainMessageReader.metadata.filename,
                    lastModifiedEpochSeconds = plainMessageReader.metadata.modTime
                )
            }
        }
    }

    private inline fun <T> decryptAndVerifyMessage(
        msg: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        validAtUtc: Long,
        crossinline block: (ExplicitVerifyMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(msg)
        val publicKeyRing = publicKeys.keyRing()
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { keyRing ->
                block(Helper.decryptExplicitVerify(pgpMessage, keyRing.value, publicKeyRing, validAtUtc))
            }
        }
    }

    private fun decryptAndVerifyDataSessionKey(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        validAtUtc: Long = 0
    ): ExplicitVerifyMessage {
        val internalSessionKey = sessionKey.toInternalSessionKey()
        val publicKeyRing = publicKeys.keyRing()
        return Helper.decryptSessionKeyExplicitVerify(data, internalSessionKey, publicKeyRing, validAtUtc)
    }

    private fun decryptAndVerifyFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        validAtUtc: Long
    ): DecryptedFile {
        val publicKeyRing = publicKeys.keyRing()
        return decryptFileSessionKey(source, destination, sessionKey, publicKeyRing, validAtUtc)
    }

    // endregion

    // region Private Sign

    private fun signMessageDetached(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored
    ): Signature {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return keyRing.value.signDetached(plainMessage).armored
            }
        }
    }

    private fun signFileDetached(
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

    private fun signMessageDetachedEncrypted(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored,
        encryptionKeyRing: KeyRing,
    ): EncryptedSignature {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return keyRing.value.signDetachedEncrypted(plainMessage, encryptionKeyRing).armored
            }
        }
    }

    private fun signFileDetachedEncrypted(
        source: File,
        unlockedKey: Unarmored,
        encryptionKeyRing: KeyRing,
    ): EncryptedSignature {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            newKey(unlockedKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    return keyRing.value.signDetachedEncryptedStream(reader, encryptionKeyRing).armored
                }
            }
        }
    }

    // endregion

    // region Private Verify

    private fun verifyMessageDetached(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = publicKey.keyRing()
        publicKeyRing.verifyDetached(plainMessage, pgpSignature, validAtUtc)
    }.isSuccess

    private fun getVerifiedTimestampMessageDetached(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Long? = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = publicKey.keyRing()
        publicKeyRing.getVerifiedSignatureTimestamp(plainMessage, pgpSignature, validAtUtc)
    }.getOrNull()

    private fun verifyMessageDetachedEncrypted(
        plainMessage: PlainMessage,
        encryptedSignature: EncryptedSignature,
        decryptionKey: Unarmored,
        publicKeys: List<Armored>,
        validAtUtc: Long
    ): Boolean = runCatching {
        val pgpSignature = PGPMessage(encryptedSignature)
        val publicKeyRing = publicKeys.keyRing()
        newKey(decryptionKey).use { key ->
            newKeyRing(key).use { keyRing ->
                publicKeyRing.verifyDetachedEncrypted(plainMessage, pgpSignature, keyRing.value, validAtUtc)
            }
        }
    }.isSuccess

    private fun verifyFileDetached(
        source: File,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long
    ): Boolean = runCatching {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            val pgpSignature = PGPSignature(signature)
            val publicKeyRing = publicKey.keyRing()
            publicKeyRing.verifyDetachedStream(reader, pgpSignature, validAtUtc)
        }
    }.isSuccess

    private fun verifyFileDetachedEncrypted(
        source: File,
        encryptedSignature: EncryptedSignature,
        decryptionKey: Unarmored,
        publicKeys: List<Armored>,
        validAtUtc: Long
    ): Boolean = runCatching {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            val pgpSignature = PGPMessage(encryptedSignature)
            val publicKeyRing = publicKeys.keyRing()
            newKey(decryptionKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    publicKeyRing.verifyDetachedEncryptedStream(reader, pgpSignature, keyRing.value, validAtUtc)
                }
            }
        }
    }.isSuccess

    // endregion

    // region Public Lock/Unlock

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
        val key = privateKey.key()
        val unlockedKey = key.unlock(passphrase)
        return GOpenPGPUnlockedKey(unlockedKey)
    }.getOrElse { throw CryptoException("PrivateKey cannot be unlocked using passphrase.", it) }

    // endregion

    // region Public Encrypt

    override fun encryptText(
        plainText: String,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encryptMessage(PlainMessage(plainText), publicKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted.", it) }

    override fun encryptData(
        data: ByteArray,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encryptMessage(PlainMessage(data), publicKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    override fun encryptData(
        data: ByteArray,
        sessionKey: SessionKey,
    ): DataPacket = runCatching {
        encryptMessageSessionKey(PlainMessage(data), sessionKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    override fun encryptFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
    ): EncryptedFile = runCatching {
        encryptFileSessionKey(source, destination, sessionKey)
    }.getOrElse { throw CryptoException("File cannot be encrypted.", it) }

    override fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSignMessage(PlainMessage(plainText), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignTextWithCompression(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSignMessageWithCompression(PlainMessage(plainText), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSignMessage(PlainMessage(data), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): DataPacket = runCatching {
        encryptAndSignMessageSessionKey(PlainMessage(data), sessionKey, unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignDataWithCompression(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage = runCatching {
        encryptAndSignMessageWithCompression(PlainMessage(data), publicKey, unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): EncryptedFile = runCatching {
        encryptAndSignFileSessionKey(source, destination, sessionKey, unlockedKey)
    }.getOrElse { throw CryptoException("File cannot be encrypted or signed.", it) }

    override fun encryptSessionKey(
        sessionKey: SessionKey,
        publicKey: Armored
    ): KeyPacket = runCatching {
        val publicKeyRing = publicKey.keyRing()
        val internalSessionKey = sessionKey.toInternalSessionKey()
        return publicKeyRing.encryptSessionKey(internalSessionKey)
    }.getOrElse { throw CryptoException("SessionKey cannot be encrypted.", it) }

    override fun encryptSessionKeyWithPassword(
        sessionKey: SessionKey,
        password: ByteArray
    ): KeyPacket = runCatching {
        val internalSessionKey = sessionKey.toInternalSessionKey()
        return Crypto.encryptSessionKeyWithPassword(internalSessionKey, password)
    }.getOrElse { throw CryptoException("SessionKey cannot be encrypted with password.", it) }

    // endregion

    // region Public Decrypt

    override fun decryptText(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): String = runCatching {
        decryptMessage(message, unlockedKey) { it.string }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptData(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        decryptMessage(message, unlockedKey) { it.binary }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptData(
        data: DataPacket,
        sessionKey: SessionKey
    ): ByteArray = runCatching {
        decryptDataSessionKey(data, sessionKey).binary
    }.getOrElse { throw CryptoException("Data cannot be decrypted.", it) }

    override fun decryptFile(
        source: File,
        destination: File,
        sessionKey: SessionKey
    ): DecryptedFile = runCatching {
        decryptFileSessionKey(source, destination, sessionKey)
    }.getOrElse { throw CryptoException("File cannot be decrypted.", it) }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime,
    ): DecryptedText = runCatching {
        decryptAndVerifyMessage(message, publicKeys, unlockedKeys, time.toUtcSeconds()) {
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
        time: VerificationTime,
    ): DecryptedData = runCatching {
        decryptAndVerifyMessage(message, publicKeys, unlockedKeys, time.toUtcSeconds()) {
            DecryptedData(
                it.message.binary,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyData(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime,
    ): DecryptedData = runCatching {
        decryptAndVerifyDataSessionKey(data, sessionKey, publicKeys, time.toUtcSeconds()).let {
            DecryptedData(
                it.message.binary,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyFile(
        source: EncryptedFile,
        destination: File,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime,
    ): DecryptedFile = runCatching {
        decryptAndVerifyFileSessionKey(source, destination, sessionKey, publicKeys, time.toUtcSeconds())
    }.getOrElse { throw CryptoException("File cannot be decrypted.", it) }

    override fun decryptSessionKey(
        keyPacket: KeyPacket,
        unlockedKey: Unarmored
    ): SessionKey = runCatching {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                SessionKey(keyRing.value.decryptSessionKey(keyPacket).key)
            }
        }
    }.getOrElse { throw CryptoException("SessionKey cannot be decrypted from KeyPacket.", it) }

    override fun decryptSessionKeyWithPassword(
        keyPacket: KeyPacket,
        password: ByteArray
    ): SessionKey = runCatching {
        SessionKey(Crypto.decryptSessionKeyWithPassword(keyPacket, password).key)
    }.getOrElse { throw CryptoException("SessionKey cannot be decrypted from KeyPacket.", it) }

    // endregion

    // region Public Sign

    override fun signText(
        plainText: String,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        signMessageDetached(PlainMessage(plainText), unlockedKey)
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }

    override fun signData(
        data: ByteArray,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        signMessageDetached(PlainMessage(data), unlockedKey)
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    override fun signFile(
        file: File,
        unlockedKey: Unarmored
    ): Signature = runCatching {
        signFileDetached(file, unlockedKey)
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    override fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
    ): EncryptedSignature = runCatching {
        signMessageDetachedEncrypted(PlainMessage(plainText), unlockedKey, encryptionKeys.keyRing())
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }

    override fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
    ): EncryptedSignature = runCatching {
        signMessageDetachedEncrypted(PlainMessage(data), unlockedKey, encryptionKeys.keyRing())
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    override fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
    ): EncryptedSignature = runCatching {
        signFileDetachedEncrypted(file, unlockedKey, encryptionKeys.keyRing())
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    // endregion

    // region Public Verify

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
    ): Boolean = verifyMessageDetached(PlainMessage(plainText), signature, publicKey, time.toUtcSeconds())

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
    ): Boolean = verifyMessageDetached(PlainMessage(data), signature, publicKey, time.toUtcSeconds())

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
    ): Boolean = verifyFileDetached(file.file, signature, publicKey, time.toUtcSeconds())

    override fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime
    ): Long? = getVerifiedTimestampMessageDetached(PlainMessage(plainText), signature, publicKey, time.toUtcSeconds())

    override fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime
    ): Long? = getVerifiedTimestampMessageDetached(PlainMessage(data), signature, publicKey, time.toUtcSeconds())

    override fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
    ): Boolean = verifyMessageDetachedEncrypted(
        PlainMessage(plainText),
        encryptedSignature,
        privateKey,
        publicKeys,
        time.toUtcSeconds()
    )

    override fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
    ): Boolean = verifyMessageDetachedEncrypted(
        PlainMessage(data),
        encryptedSignature,
        privateKey,
        publicKeys,
        time.toUtcSeconds()
    )

    override fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
    ): Boolean = verifyFileDetachedEncrypted(file, encryptedSignature, privateKey, publicKeys, time.toUtcSeconds())

    // endregion

    // region Public Get

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
        privateKey.key().armoredPublicKey
    }.getOrElse { throw CryptoException("Public key cannot be extracted from privateKey.", it) }

    override fun getFingerprint(
        key: Armored
    ): String = runCatching {
        key.key().fingerprint
    }.getOrElse { throw CryptoException("Fingerprint cannot be extracted from key.", it) }

    override fun getJsonSHA256Fingerprints(
        key: Armored
    ): String = runCatching {
        Helper.getJsonSHA256Fingerprints(key).toString(Charsets.UTF_8)
    }.getOrElse { throw CryptoException("SHA256 Fingerprints cannot be extracted from key.", it) }

    override fun getBase64Encoded(array: ByteArray): String {
        return Base64.encodeToString(array, Base64.DEFAULT)
    }

    override fun getBase64Decoded(string: String): ByteArray {
        return Base64.decode(string)
    }

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

    // endregion

    // region Public SessionKey/HashKey/PrivateKey/Token generation

    override fun generateNewSessionKey(): SessionKey {
        return SessionKey(Crypto.generateSessionKey().key)
    }

    override fun generateNewHashKey(): HashKey {
        val secret = Crypto.randomToken(32)
        require(secret.size == 32)
        val token = getBase64Encoded(secret).toByteArray(Charsets.UTF_8)
        return HashKey(token, VerificationStatus.NotSigned)
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

    override fun updatePrivateKeyPassphrase(
        privateKey: String,
        passphrase: ByteArray,
        newPassphrase: ByteArray
    ): Armored = runCatching {
        check(passphrase.isNotEmpty()) { "The current passphrase key can't be empty." }
        check(newPassphrase.isNotEmpty()) { "The new passphrase for generating key can't be empty." }
        checkNotNull(unlockOrNull(privateKey, passphrase)) { "The passphrase cannot unlock the private key." }
        Helper.updatePrivateKeyPassphrase(privateKey, passphrase, newPassphrase)
    }.getOrElse { throw CryptoException("Passphrase cannot be changed for Private Key.", it) }

    // endregion

    // region Public Time

    override fun updateTime(epochSeconds: Long) {
        Crypto.updateTime(epochSeconds)
    }

    // endregion

    companion object {
        // 32K is usually not far from the optimal buffer size on Android devices.
        const val DEFAULT_BUFFER_SIZE = 32768

        const val KEY_CACHE_ENABLED = false
        const val KEY_CACHE_LRU_MAX_SIZE = 100
    }
}
