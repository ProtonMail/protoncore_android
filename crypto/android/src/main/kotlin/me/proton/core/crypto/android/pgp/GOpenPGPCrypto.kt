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

import android.util.Base64
import androidx.core.util.lruCache
import com.proton.gopenpgp.armor.Armor
import com.proton.gopenpgp.constants.Constants
import com.proton.gopenpgp.crypto.Crypto
import com.proton.gopenpgp.crypto.Key
import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.PGPMessage
import com.proton.gopenpgp.crypto.PGPSignature
import com.proton.gopenpgp.crypto.PGPSplitMessage
import com.proton.gopenpgp.crypto.PlainMessage
import com.proton.gopenpgp.crypto.PlainMessageMetadata
import com.proton.gopenpgp.crypto.SigningContext
import com.proton.gopenpgp.crypto.VerificationContext as GolangVerificationContext
import com.proton.gopenpgp.helper.ExplicitVerifyMessage
import com.proton.gopenpgp.helper.Go2AndroidReader
import com.proton.gopenpgp.helper.Helper
import com.proton.gopenpgp.helper.Mobile2GoReader
import com.proton.gopenpgp.helper.Mobile2GoWriter
import com.proton.gopenpgp.srp.Srp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.pgp.trimLinesEnd
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedMimeMessage
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.pgp.HashKey
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.PacketType
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationContext
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

    override fun serializeKeys(keys: List<Unarmored>): ByteArray =
        newKeyRing(newKeys(keys)).use { it.value.serialize() }

    override fun deserializeKeys(keys: ByteArray): List<Unarmored> =
        CloseableUnlockedKeyRing(Crypto.newKeyRingFromBinary(keys)).use { keyRing ->
            val count = keyRing.value.countEntities()
            val indices = 0..<count
            indices.map { index -> keyRing.value.getKey(index).serialize() }
        }

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

    private data class SignatureParameters(
        val signingUnlockedKey: Unarmored,
        val signatureContext: SignatureContext?
    )

    private fun encryptMessage(
        plainMessage: PlainMessage,
        publicKey: Armored,
    ): EncryptedMessage {
        val publicKeyRing = publicKey.keyRing()
        return publicKeyRing.encrypt(plainMessage, null).armored
    }

    private fun encryptAndSignMessage(
        plainMessage: PlainMessage,
        publicKey: Armored,
        signatureParameters: SignatureParameters
    ): EncryptedMessage {
        val publicKeyRing = publicKey.keyRing()
        return newKey(signatureParameters.signingUnlockedKey).use { key ->
            newKeyRing(key).use { signKeyRing ->
                val context = signatureParameters.signatureContext?.toGolang()
                publicKeyRing.encryptWithContext(plainMessage, signKeyRing.value, context).armored
            }
        }
    }

    private fun encryptAndSignMessageWithCompression(
        plainMessage: PlainMessage,
        publicKey: Armored,
        signatureParameters: SignatureParameters
    ): EncryptedMessage {
        val publicKeyRing = publicKey.keyRing()
        return newKey(signatureParameters.signingUnlockedKey).use { key ->
            newKeyRing(key).use { signKeyRing ->
                val context = signatureParameters.signatureContext?.toGolang()
                publicKeyRing.encryptWithContextAndCompression(plainMessage, signKeyRing.value, context).armored
            }
        }
    }

    private fun encryptMessageSessionKey(
        plainMessage: PlainMessage,
        sessionKey: SessionKey,
    ): DataPacket = sessionKey.toInternalSessionKey().encrypt(plainMessage)

    private fun encryptAndSignMessageSessionKey(
        plainMessage: PlainMessage,
        sessionKey: SessionKey,
        signatureParameters: SignatureParameters
    ): DataPacket {
        return sessionKey.toInternalSessionKey().let { internalSessionKey ->
            newKey(signatureParameters.signingUnlockedKey).use { key ->
                newKeyRing(key).use { signKeyRing ->
                    val context = signatureParameters.signatureContext?.toGolang()
                    internalSessionKey.encryptAndSignWithContext(plainMessage, signKeyRing.value, context)
                }
            }
        }
    }

    private fun encryptFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey
    ): EncryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val writer = Mobile2GoWriter(fileOutputStream.writer())
                val plainMessageMetadata = PlainMessageMetadata(true, source.name, source.lastModified() / 1000)
                val internalSessionKey = sessionKey.toInternalSessionKey()
                val writeCloser = internalSessionKey.encryptStream(writer, plainMessageMetadata, null)
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
        signatureParameters: SignatureParameters
    ): EncryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                newKey(signatureParameters.signingUnlockedKey).use { key ->
                    newKeyRing(key).use { signKeyRing ->
                        val writer = Mobile2GoWriter(fileOutputStream.writer())
                        val plainMessageMetadata = PlainMessageMetadata(true, source.name, source.lastModified() / 1000)
                        val internalSessionKey = sessionKey.toInternalSessionKey()
                        val context = signatureParameters.signatureContext?.toGolang()
                        val writeCloser = internalSessionKey.encryptStreamWithContext(
                            writer,
                            plainMessageMetadata,
                            signKeyRing.value,
                            context
                        )
                        fileInputStream.reader().copyTo(writeCloser)
                        writeCloser.close()
                        return destination
                    }
                }

            }
        }
    }

    private fun encryptEncryptedMessageToAdditionalKey(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        publicKey: Armored,
    ): EncryptedMessage {
        val pgpSplitMessage = PGPSplitMessage(message)
        val publicKeyRing = publicKey.keyRing()
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                Helper.encryptPGPMessageToAdditionalKey(pgpSplitMessage, keyRing.value, publicKeyRing)
                return pgpSplitMessage.armored
            }
        }
    }

    // endregion

    // region Private Decrypt

    private data class VerificationParameters(
        val verificationKeys: List<Armored>,
        val validAtUtc: Long,
        val verificationContext: VerificationContext?,
    )

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
        sessionKey: SessionKey
    ): PlainMessage {
        val internalSessionKey = sessionKey.toInternalSessionKey()
        return internalSessionKey.decrypt(data)
    }

    private fun decryptFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
    ): DecryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val reader = Mobile2GoReader(fileInputStream.mobileReader())
                val internalSessionKey = sessionKey.toInternalSessionKey()
                val plainMessageReader = internalSessionKey.decryptStream(
                    reader,
                    null,
                    0
                )
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

    private fun decryptAndVerifyFileSessionKey(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        verificationParameters: VerificationParameters
    ): DecryptedFile {
        source.inputStream().use { fileInputStream ->
            destination.outputStream().use { fileOutputStream ->
                val reader = Mobile2GoReader(fileInputStream.mobileReader())
                val internalSessionKey = sessionKey.toInternalSessionKey()
                val plainMessageReader = internalSessionKey.decryptStreamWithContext(
                    reader,
                    verificationParameters.verificationKeys.keyRing(),
                    verificationParameters.validAtUtc,
                    verificationParameters.verificationContext?.toGolang()
                )
                Go2AndroidReader(plainMessageReader).copyTo(fileOutputStream.writer())
                return DecryptedFile(
                    file = destination,
                    status = Helper.verifySignatureExplicit(plainMessageReader).toVerificationStatus(),
                    filename = plainMessageReader.metadata.filename,
                    lastModifiedEpochSeconds = plainMessageReader.metadata.modTime
                )
            }
        }
    }

    private inline fun <T> decryptAndVerifyMessage(
        msg: EncryptedMessage,
        unlockedKeys: List<Unarmored>,
        verificationParameters: VerificationParameters,
        crossinline block: (ExplicitVerifyMessage) -> T
    ): T {
        val pgpMessage = Crypto.newPGPMessageFromArmored(msg)
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { keyRing ->
                val decryptResult = Helper.decryptExplicitVerifyWithContext(
                    pgpMessage,
                    keyRing.value,
                    verificationParameters.verificationKeys.keyRing(),
                    verificationParameters.validAtUtc,
                    verificationParameters.verificationContext?.toGolang()
                )
                block(decryptResult)
            }
        }
    }

    private fun decryptAndVerifyDataSessionKey(
        data: DataPacket,
        sessionKey: SessionKey,
        verificationParameters: VerificationParameters
    ): ExplicitVerifyMessage {
        val internalSessionKey = sessionKey.toInternalSessionKey()
        return Helper.decryptSessionKeyExplicitVerifyWithContext(
            data,
            internalSessionKey,
            verificationParameters.verificationKeys.keyRing(),
            verificationParameters.validAtUtc,
            verificationParameters.verificationContext?.toGolang()
        )
    }

    // endregion

    // region Private Sign

    private fun SignatureContext.toGolang() = SigningContext(
        value,
        isCritical
    )

    private fun signMessageDetached(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): Signature {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                return keyRing.value.signDetachedWithContext(
                    plainMessage,
                    signatureContext?.toGolang()
                ).armored
            }
        }
    }

    private fun signFileDetached(
        source: File,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): Signature {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            newKey(unlockedKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    return keyRing.value.signDetachedStreamWithContext(reader, signatureContext?.toGolang()).armored
                }
            }
        }
    }

    private fun signMessageDetachedEncrypted(
        plainMessage: PlainMessage,
        unlockedKey: Unarmored,
        encryptionKeyRing: KeyRing,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        newKey(unlockedKey).use { key ->
            newKeyRing(key).use { keyRing ->
                val signature = keyRing.value.signDetachedWithContext(plainMessage, signatureContext?.toGolang())
                return encryptionKeyRing.encrypt(PlainMessage(signature.data), null).armored
            }
        }
    }

    private fun signFileDetachedEncrypted(
        source: File,
        unlockedKey: Unarmored,
        encryptionKeyRing: KeyRing,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            newKey(unlockedKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    val signature = keyRing.value.signDetachedStreamWithContext(reader, signatureContext?.toGolang())
                    return encryptionKeyRing.encrypt(PlainMessage(signature.data), null).armored
                }
            }
        }
    }

    // endregion

    // region Private Verify

    private fun VerificationContext.toGolang(): GolangVerificationContext {
        val required = this.required
        return GolangVerificationContext(
            value,
            required is VerificationContext.ContextRequirement.Required,
            if (required is VerificationContext.ContextRequirement.Required.After) required.timestamp else 0
        )
    }


    private fun verifyMessageDetached(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long,
        verificationContext: VerificationContext?
    ): Boolean = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = publicKey.keyRing()
        publicKeyRing.verifyDetachedWithContext(plainMessage, pgpSignature, validAtUtc, verificationContext?.toGolang())
    }.isSuccess

    private fun getVerifiedTimestampMessageDetached(
        plainMessage: PlainMessage,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long,
        verificationContext: VerificationContext?
    ): Long? = runCatching {
        val pgpSignature = PGPSignature(signature)
        val publicKeyRing = publicKey.keyRing()
        publicKeyRing.getVerifiedSignatureTimestampWithContext(
            plainMessage,
            pgpSignature,
            validAtUtc,
            verificationContext?.toGolang()
        )
    }.getOrNull()

    @SuppressWarnings("LongParameterList")
    private fun verifyMessageDetachedEncrypted(
        plainMessage: PlainMessage,
        encryptedSignature: EncryptedSignature,
        decryptionKey: Unarmored,
        publicKeys: List<Armored>,
        validAtUtc: Long,
        verificationContext: VerificationContext?
    ): Boolean = runCatching {
        val publicKeyRing = publicKeys.keyRing()
        newKey(decryptionKey).use { key ->
            newKeyRing(key).use { keyRing ->
                val decryptedSignature = keyRing.value.decrypt(PGPMessage(encryptedSignature), null, 0).let {
                    PGPSignature(it.data)
                }
                publicKeyRing.verifyDetachedWithContext(
                    plainMessage,
                    decryptedSignature,
                    validAtUtc,
                    verificationContext?.toGolang()
                )
            }
        }
    }.isSuccess

    private fun verifyFileDetached(
        source: File,
        signature: Armored,
        publicKey: Armored,
        validAtUtc: Long,
        verificationContext: VerificationContext?
    ): Boolean = runCatching {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            val pgpSignature = PGPSignature(signature)
            val publicKeyRing = publicKey.keyRing()
            publicKeyRing.verifyDetachedStreamWithContext(
                reader,
                pgpSignature,
                validAtUtc,
                verificationContext?.toGolang()
            )
        }
    }.isSuccess

    @SuppressWarnings("LongParameterList")
    private fun verifyFileDetachedEncrypted(
        source: File,
        encryptedSignature: EncryptedSignature,
        decryptionKey: Unarmored,
        publicKeys: List<Armored>,
        validAtUtc: Long,
        verificationContext: VerificationContext?
    ): Boolean = runCatching {
        source.inputStream().use { fileInputStream ->
            val reader = Mobile2GoReader(fileInputStream.mobileReader())
            val publicKeyRing = publicKeys.keyRing()
            newKey(decryptionKey).use { key ->
                newKeyRing(key).use { keyRing ->
                    val decryptedSignature = keyRing.value.decrypt(PGPMessage(encryptedSignature), null, 0).let {
                        PGPSignature(it.data)
                    }
                    publicKeyRing.verifyDetachedStreamWithContext(
                        reader,
                        decryptedSignature,
                        validAtUtc,
                        verificationContext?.toGolang()
                    )
                }
            }
        }
    }.isSuccess

    // endregion

    // region isPrivateKey/isPublicKey/isValidKey

    override fun isPublicKey(key: Armored): Boolean = runCatching { key.key().isPrivate.not() }.getOrDefault(false)
    override fun isPrivateKey(key: Armored): Boolean = runCatching { key.key().isPrivate }.getOrDefault(false)
    override fun isValidKey(key: Armored): Boolean = runCatching { key.key(); true }.getOrDefault(false)

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

    override fun encryptTextWithPassword(
        text: String,
        password: ByteArray
    ): EncryptedMessage = runCatching {
        Crypto.encryptMessageWithPassword(PlainMessage(text), password).splitMessage().armored
    }.getOrElse { throw CryptoException("Text cannot be encrypted with password.", it) }

    override fun encryptData(
        data: ByteArray,
        publicKey: Armored
    ): EncryptedMessage = runCatching {
        encryptMessage(PlainMessage(data), publicKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    override fun encryptData(
        data: ByteArray,
        sessionKey: SessionKey
    ): DataPacket = runCatching {
        encryptMessageSessionKey(PlainMessage(data), sessionKey)
    }.getOrElse { throw CryptoException("Data cannot be encrypted.", it) }

    override fun encryptDataWithPassword(
        data: ByteArray,
        password: ByteArray
    ): EncryptedMessage = runCatching {
        Crypto.encryptMessageWithPassword(PlainMessage(data), password).splitMessage().armored
    }.getOrElse { throw CryptoException("Data cannot be encrypted with password.", it) }

    override fun encryptFile(
        source: File,
        destination: File,
        sessionKey: SessionKey
    ): EncryptedFile = runCatching {
        encryptFileSessionKey(source, destination, sessionKey)
    }.getOrElse { throw CryptoException("File cannot be encrypted.", it) }

    override fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignMessage(PlainMessage(plainText), publicKey, signatureParameters)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignTextWithCompression(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignMessageWithCompression(PlainMessage(plainText), publicKey, signatureParameters)
    }.getOrElse { throw CryptoException("PlainText cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignMessage(PlainMessage(data), publicKey, signatureParameters)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignData(
        data: ByteArray,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): DataPacket = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignMessageSessionKey(PlainMessage(data), sessionKey, signatureParameters)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignDataWithCompression(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedMessage = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignMessageWithCompression(PlainMessage(data), publicKey, signatureParameters)
    }.getOrElse { throw CryptoException("Data cannot be encrypted or signed.", it) }

    override fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): EncryptedFile = runCatching {
        val signatureParameters = SignatureParameters(unlockedKey, signatureContext)
        encryptAndSignFileSessionKey(source, destination, sessionKey, signatureParameters)
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

    override fun encryptMessageToAdditionalKey(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        publicKey: Armored,
    ): EncryptedMessage = runCatching {
        encryptEncryptedMessageToAdditionalKey(message, unlockedKey, publicKey)
    }.getOrElse { throw CryptoException("EncryptedMessage cannot be encrypted to additional key.", it) }

    // endregion

    // region Public Decrypt

    override fun decryptText(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): String = runCatching {
        decryptMessage(message, unlockedKey) { it.string }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptTextWithPassword(
        message: EncryptedMessage,
        password: ByteArray
    ): String = runCatching {
        Crypto.decryptMessageWithPassword(PGPMessage(message), password).string
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptMimeMessage(
        message: EncryptedMessage,
        unlockedKeys: List<Unarmored>
    ): DecryptedMimeMessage = runCatching {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        return newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { decryptionKeyRing ->
                DecryptMimeMessage().invoke(
                    pgpMessage,
                    decryptionKeyRing.value,
                    verificationKeyRing = null,
                    verificationTime = 0
                )
            }
        }
    }.getOrElse { throw CryptoException("Mime message cannot be decrypted.", it) }

    override fun decryptData(
        message: EncryptedMessage,
        unlockedKey: Unarmored
    ): ByteArray = runCatching {
        decryptMessage(message, unlockedKey) { it.getBinaryOrEmpty() }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptData(
        data: DataPacket,
        sessionKey: SessionKey
    ): ByteArray = runCatching {
        decryptDataSessionKey(data, sessionKey).getBinaryOrEmpty()
    }.getOrElse { throw CryptoException("Data cannot be decrypted.", it) }

    override fun decryptDataWithPassword(
        message: EncryptedMessage,
        password: ByteArray
    ): ByteArray = runCatching {
        Crypto.decryptMessageWithPassword(PGPMessage(message), password).data
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

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
        verificationContext: VerificationContext?
    ): DecryptedText = runCatching {
        val verificationParameters = VerificationParameters(publicKeys, time.toUtcSeconds(), verificationContext)
        decryptAndVerifyMessage(message, unlockedKeys, verificationParameters) {
            DecryptedText(
                it.message.string,
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyMimeMessage(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime
    ): DecryptedMimeMessage = runCatching {
        val pgpMessage = Crypto.newPGPMessageFromArmored(message)
        val verificationKeyRing = publicKeys.keyRing()
        newKeys(unlockedKeys).use { keys ->
            newKeyRing(keys).use { decryptionKeyRing ->
                DecryptMimeMessage().invoke(
                    pgpMessage,
                    decryptionKeyRing.value,
                    verificationKeyRing,
                    time.toUtcSeconds()
                )
            }
        }
    }.getOrElse { throw CryptoException("Mime message cannot be decrypted.", it) }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedData = runCatching {
        val verificationParameters = VerificationParameters(publicKeys, time.toUtcSeconds(), verificationContext)
        decryptAndVerifyMessage(message, unlockedKeys, verificationParameters) {
            DecryptedData(
                it.message.getBinaryOrEmpty(),
                it.signatureVerificationError.toVerificationStatus()
            )
        }
    }.getOrElse { throw CryptoException("Message cannot be decrypted.", it) }

    override fun decryptAndVerifyData(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): DecryptedData = runCatching {
        val verificationParameters = VerificationParameters(publicKeys, time.toUtcSeconds(), verificationContext)
        decryptAndVerifyDataSessionKey(data, sessionKey, verificationParameters).let {
            DecryptedData(
                it.message.getBinaryOrEmpty(),
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
        verificationContext: VerificationContext?
    ): DecryptedFile = runCatching {
        val verificationParameters = VerificationParameters(publicKeys, time.toUtcSeconds(), verificationContext)
        decryptAndVerifyFileSessionKey(source, destination, sessionKey, verificationParameters)
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
        unlockedKey: Unarmored,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): Signature = runCatching {
        val plainTextTrimmed = plainText.trimLinesEndIf { trimTrailingSpaces }
        signMessageDetached(PlainMessage(plainTextTrimmed), unlockedKey, signatureContext)
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }

    override fun signData(
        data: ByteArray,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): Signature = runCatching {
        signMessageDetached(PlainMessage(data), unlockedKey, signatureContext)
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    override fun signFile(
        file: File,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): Signature = runCatching {
        signFileDetached(file, unlockedKey, signatureContext)
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    override fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): EncryptedSignature = runCatching {
        val plainTextTrimmed = plainText.trimLinesEndIf { trimTrailingSpaces }
        signMessageDetachedEncrypted(
            PlainMessage(plainTextTrimmed),
            unlockedKey,
            encryptionKeys.keyRing(),
            signatureContext
        )
    }.getOrElse { throw CryptoException("PlainText cannot be signed.", it) }


    override fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext?
    ): EncryptedSignature = runCatching {
        signMessageDetachedEncrypted(PlainMessage(data), unlockedKey, encryptionKeys.keyRing(), signatureContext)
    }.getOrElse { throw CryptoException("Data cannot be signed.", it) }

    override fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext?
    ): EncryptedSignature = runCatching {
        signFileDetachedEncrypted(file, unlockedKey, encryptionKeys.keyRing(), signatureContext)
    }.getOrElse { throw CryptoException("InputStream cannot be signed.", it) }

    // endregion

    // region Public Verify

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        val plainTextTrimmed = plainText.trimLinesEndIf { trimTrailingSpaces }
        return verifyMessageDetached(
            PlainMessage(plainTextTrimmed),
            signature,
            publicKey,
            time.toUtcSeconds(),
            verificationContext
        )
    }

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean = verifyMessageDetached(
        PlainMessage(data),
        signature,
        publicKey,
        time.toUtcSeconds(),
        verificationContext
    )

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean = verifyFileDetached(file.file, signature, publicKey, time.toUtcSeconds(), verificationContext)

    override fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Long? {
        val plainTextTrimmed = plainText.trimLinesEndIf { trimTrailingSpaces }
        return getVerifiedTimestampMessageDetached(
            PlainMessage(plainTextTrimmed),
            signature,
            publicKey,
            time.toUtcSeconds(),
            verificationContext
        )
    }

    override fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Long? = getVerifiedTimestampMessageDetached(
        PlainMessage(data),
        signature,
        publicKey,
        time.toUtcSeconds(),
        verificationContext
    )

    override fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        val plainTextTrimmed = plainText.trimLinesEndIf { trimTrailingSpaces }
        return verifyMessageDetachedEncrypted(
            PlainMessage(plainTextTrimmed),
            encryptedSignature,
            privateKey,
            publicKeys,
            time.toUtcSeconds(),
            verificationContext
        )
    }

    override fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean = verifyMessageDetachedEncrypted(
        PlainMessage(data),
        encryptedSignature,
        privateKey,
        publicKeys,
        time.toUtcSeconds(),
        verificationContext
    )

    override fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean = verifyFileDetachedEncrypted(
        file,
        encryptedSignature,
        privateKey,
        publicKeys,
        time.toUtcSeconds(),
        verificationContext
    )

    // endregion

    // region Public Get

    override fun getArmored(
        data: Unarmored,
        header: PGPHeader
    ): Armored = runCatching {
        Armor.armorWithType(
            /* bytes  */
            data,
            /* header */
            when (header) {
                PGPHeader.Message -> Constants.PGPMessageHeader
                PGPHeader.Signature -> Constants.PGPSignatureHeader
                PGPHeader.PublicKey -> Constants.PublicKeyHeader
                PGPHeader.PrivateKey -> Constants.PrivateKeyHeader
            }
        )
    }.getOrElse { throw CryptoException("Armored cannot be extracted from Unarmored.", it) }

    override fun getUnarmored(
        data: Armored
    ): Unarmored = runCatching {
        Armor.unarmor(data)
    }.getOrElse { throw CryptoException("Unarmored cannot be extracted from Armored.", it) }

    override fun getEncryptedPackets(
        message: EncryptedMessage
    ): List<EncryptedPacket> = runCatching {
        val pgpSplitMessage = PGPSplitMessage(message)
        var numberOfKeyPackets = pgpSplitMessage.getNumberOfKeyPackets().toInt()
        return listOf(
            EncryptedPacket(pgpSplitMessage.keyPacket, PacketType.Key, numberOfKeyPackets),
            EncryptedPacket(pgpSplitMessage.dataPacket, PacketType.Data)
        )
    }.getOrElse { throw CryptoException("EncryptedFile cannot be extracted from EncryptedMessage.", it) }

    override fun getPublicKey(
        privateKey: Armored
    ): Armored = runCatching {
        privateKey.key().armoredPublicKey
    }.getOrElse { throw CryptoException("Public key cannot be extracted from privateKey.", it) }

    override fun isKeyExpired(key: Armored): Boolean = runCatching {
        key.key().isExpired
    }.getOrElse { throw CryptoException("Key can not be checked for expiration.", it) }

    override fun isKeyRevoked(key: Armored): Boolean = runCatching {
        key.key().isRevoked
    }.getOrElse { throw CryptoException("Key can not be checked for revocation.", it) }

    override fun getFingerprint(
        key: Armored
    ): String = runCatching {
        key.key().fingerprint
    }.getOrElse { throw CryptoException("Fingerprint cannot be extracted from key.", it) }

    override fun getSHA256Fingerprint(
        key: Armored
    ): String = runCatching {
        key.key().shA256Fingerprint
    }.getOrElse { throw CryptoException("SHA256 Fingerprint cannot be extracted from key.", it) }

    override fun getJsonSHA256Fingerprints(
        key: Armored
    ): String = runCatching {
        Helper.getJsonSHA256Fingerprints(key).toString(Charsets.UTF_8)
    }.getOrElse { throw CryptoException("SHA256 Fingerprints cannot be extracted from key.", it) }

    override fun getBase64Encoded(array: ByteArray): Based64Encoded =
        Base64.encodeToString(array, Base64.DEFAULT)

    override fun getBase64EncodedNoWrap(array: ByteArray): Based64Encoded =
        Base64.encodeToString(array, Base64.NO_WRAP)

    override fun getBase64Decoded(string: Based64Encoded): ByteArray =
        Base64.decode(string, Base64.DEFAULT)

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

    override fun generateNewSessionKey(): SessionKey =
        SessionKey(Crypto.generateSessionKey().key)

    override fun generateNewHashKey(): HashKey {
        val secret = Crypto.randomToken(32)
        require(secret.size == 32)
        val token = getBase64Encoded(secret).toByteArray(Charsets.UTF_8)
        return HashKey(token, VerificationStatus.NotSigned)
    }

    override fun generateNewKeySalt(): String {
        val salt = generateRandomBytes(16)
        val keySalt = Base64.encodeToString(salt, Base64.DEFAULT)
        // Truncate newline character.
        return keySalt.substring(0, keySalt.length - 1)
    }

    override fun generateNewToken(size: Int): ByteArray {
        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
        val secret = generateRandomBytes(size)
        val token = secret.toHexString().toByteArray(Charsets.UTF_8)
        require(token.size == secret.size * 2)
        return token
    }

    override fun generateRandomBytes(size: Int): ByteArray {
        val secret = ByteArray(size)
        SecureRandom().nextBytes(secret)
        require(size == secret.size)
        return secret
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

    override suspend fun getCurrentTime(): Long = withContext(Dispatchers.IO) {
        Crypto.getUnixTime()
    }

    // endregion

    private fun String.trimLinesEndIf(
        predicate: () -> Boolean
    ): String = if (predicate.invoke()) trimLinesEnd() else this


    /**
     * When gopenpgp decrypts an empty message, getBinary() will return null.
     * We map the null value to an empty ByteArray()
     */
    private fun PlainMessage.getBinaryOrEmpty(): ByteArray = binary ?: ByteArray(0)

    companion object {
        // 32K is usually not far from the optimal buffer size on Android devices.
        const val DEFAULT_BUFFER_SIZE = 32_768

        const val KEY_CACHE_ENABLED = false
        const val KEY_CACHE_LRU_MAX_SIZE = 100
    }
}
