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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
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
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.decryptAndVerifyDataOrNull
import me.proton.core.crypto.common.pgp.decryptAndVerifyTextOrNull
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import me.proton.core.util.kotlin.CoreLogger

/**
 * Executes the given [block] function for a [KeyHolder] on a [KeyHolderContext] and then close any associated
 * key resources whether an exception is thrown or not.
 *
 * Example:
 * ```
 * keyholder.useKeys(context) {
 *     val text = "text"
 *
 *     val encryptedText = encryptText(text)
 *     val signedText = signText(text)
 *
 *     val decryptedText = decryptText(encryptedText)
 *     val isVerified = verifyText(decryptedText, signedText)
 * }
 * ```
 * @param context [CryptoContext] providing any needed dependencies for Crypto functions.
 * @param block a function allowing usage of [KeyHolderContext] extension functions.
 * @return the result of [block] function invoked on this [KeyHolder].
 * @see [useKeysAs]
 */
inline fun <R> KeyHolder.useKeys(context: CryptoContext, block: KeyHolderContext.() -> R): R {
    val activeKeys = keys.filter { it.privateKey.isActive }
    val privateKeys = activeKeys.map { key -> key.privateKey }
    val publicKeys = privateKeys.map { key -> key.publicKey(context) }
    val keyHolderContext = KeyHolderContext(
        context = context,
        privateKeyRing = PrivateKeyRing(context, privateKeys),
        publicKeyRing = PublicKeyRing(publicKeys)
    )
    return keyHolderContext.use { block(it) }
}

/**
 * Same as [useKeys] but without receiver, useful for nested keys usage.
 *
 * Executes the given [block] function for a [KeyHolder] on a [KeyHolderContext] and then close any associated
 * key resources whether an exception is thrown or not.
 *
 * Example:
 * ```
 * main.useKeysAs(context) { mainContext ->
 *     nested.useKeysAs(context) { nestedContext ->
 *         val text = "text"
 *
 *         val encryptedText = nestedContext.encryptText(text)
 *         val signedText = mainContext.signText(text)
 *     }
 * }
 * ```
 * @param context [CryptoContext] providing any needed dependencies for Crypto functions.
 * @param block a function allowing usage of [KeyHolderContext] extension functions.
 * @return the result of [block] function invoked on this [KeyHolder].
 * @see [useKeys]
 */
inline fun <R> KeyHolder.useKeysAs(context: CryptoContext, block: (KeyHolderContext) -> R): R = useKeys(context, block)

/**
 * Decrypt [message] as [String] using [PrivateKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptTextOrNull]
 * @see [KeyHolderContext.encryptText]
 */
fun KeyHolderContext.decryptText(message: EncryptedMessage): String =
    privateKeyRing.decryptText(message)

/**
 * Decrypt [message] as [ByteArray] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptDataOrNull]
 * @see [KeyHolderContext.encryptData]
 */
fun KeyHolderContext.decryptData(message: EncryptedMessage): ByteArray =
    privateKeyRing.decryptData(message)

/**
 * Decrypt [data] as [ByteArray] using [keyPacket].
 *
 * @throws [CryptoException] if [data] cannot be decrypted.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptDataOrNull]
 * @see [KeyHolderContext.encryptData]
 */
fun KeyHolderContext.decryptData(data: DataPacket, keyPacket: KeyPacket): ByteArray =
    decryptSessionKey(keyPacket).use { key -> key.decryptData(context, data) }

/**
 * Decrypt [data] as [ByteArray] using [sessionKey].
 *
 * @throws [CryptoException] if [data] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptDataOrNull]
 * @see [KeyHolderContext.encryptData]
 */
fun KeyHolderContext.decryptData(data: DataPacket, sessionKey: SessionKey): ByteArray =
    sessionKey.decryptData(context, data)

/**
 * Decrypt [source] into [destination] as [DecryptedFile] using [keyPacket].
 *
 * @throws [CryptoException] if [source] cannot be decrypted.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptFileOrNull]
 * @see [KeyHolderContext.encryptFile]
 */
fun KeyHolderContext.decryptFile(source: EncryptedFile, destination: File, keyPacket: KeyPacket): DecryptedFile =
    decryptSessionKey(keyPacket).use { key -> decryptFile(source, destination, key) }

/**
 * Decrypt [source] into [destination] as [DecryptedFile] using [sessionKey].
 *
 * @throws [CryptoException] if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptFileOrNull]
 * @see [KeyHolderContext.encryptFile]
 */
fun KeyHolderContext.decryptFile(source: EncryptedFile, destination: File, sessionKey: SessionKey): DecryptedFile =
    sessionKey.decryptFile(context, source, destination)

/**
 * Decrypt [keyPacket] as [SessionKey] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [keyPacket] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptSessionKeyOrNull]
 * @see [KeyHolderContext.encryptSessionKey]
 */
fun KeyHolderContext.decryptSessionKey(keyPacket: KeyPacket): SessionKey =
    privateKeyRing.decryptSessionKey(keyPacket)

/**
 * Decrypt [hashKey] as [HashKey] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 *
 * @throws [CryptoException] if [hashKey] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyHashKeyOrNull]
 * @see [KeyHolderContext.encryptAndSignHashKey]
 */
fun KeyHolderContext.decryptAndVerifyHashKey(
    hashKey: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing
): HashKey = decryptAndVerifyData(hashKey, verifyKeyRing).let {
    HashKey(it.data, it.status)
}

/**
 * Decrypt [message] as [String] using [PrivateKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptText]
 */
fun KeyHolderContext.decryptTextOrNull(message: EncryptedMessage): String? =
    privateKeyRing.decryptTextOrNull(message)

/**
 * Decrypt [message] as [ByteArray] using [PrivateKeyRing].
 *
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun KeyHolderContext.decryptDataOrNull(message: EncryptedMessage): ByteArray? =
    privateKeyRing.decryptDataOrNull(message)

/**
 * Decrypt [data] as [ByteArray] using [PrivateKeyRing].
 *
 * @return [ByteArray], or `null` if [data] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun KeyHolderContext.decryptDataOrNull(data: DataPacket, sessionKey: SessionKey): ByteArray? =
    context.pgpCrypto.decryptDataOrNull(data, sessionKey)

/**
 * Decrypt [source] into [destination] as [DecryptedFile] using [keyPacket].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptFileOrNull]
 * @see [KeyHolderContext.encryptFile]
 */
fun KeyHolderContext.decryptFileOrNull(source: EncryptedFile, destination: File, keyPacket: KeyPacket): DecryptedFile? =
    decryptSessionKeyOrNull(keyPacket)?.use { key -> decryptFileOrNull(source, destination, key) }

/**
 * Decrypt [source] into [destination] as [DecryptedFile] using [sessionKey].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptFileOrNull]
 * @see [KeyHolderContext.encryptFile]
 */
fun KeyHolderContext.decryptFileOrNull(
    source: EncryptedFile,
    destination: File,
    sessionKey: SessionKey
): DecryptedFile? = sessionKey.decryptFileOrNull(context, source, destination)

/**
 * Decrypt [keyPacket] as [SessionKey] using [PrivateKeyRing].
 *
 * @return [SessionKey], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptSessionKey]
 */
fun KeyHolderContext.decryptSessionKeyOrNull(keyPacket: KeyPacket): SessionKey? =
    privateKeyRing.decryptSessionKeyOrNull(keyPacket)

/**
 * Decrypt [hashKey] as [HashKey] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 *
 * @return [HashKey], or `null` if [hashKey] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyHashKey]
 */
fun KeyHolderContext.decryptAndVerifyHashKeyOrNull(
    hashKey: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing
): HashKey? = runCatching { decryptAndVerifyHashKey(hashKey, verifyKeyRing) }.getOrNull()

/**
 * Sign [text] using [PrivateKeyRing].
 *
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [KeyHolderContext.verifyText]
 */
fun KeyHolderContext.signText(text: String, trimTrailingSpaces: Boolean = true): Signature =
    privateKeyRing.signText(text, trimTrailingSpaces)

/**
 * Sign [data] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [KeyHolderContext.verifyData]
 */
fun KeyHolderContext.signData(data: ByteArray): Signature =
    privateKeyRing.signData(data)

/**
 * Sign [file] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [KeyHolderContext.verifyFile]
 */
fun KeyHolderContext.signFile(file: File): Signature =
    privateKeyRing.signFile(file)

/**
 * Sign [text] using [PrivateKeyRing]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [KeyHolderContext.verifyTextEncrypted]
 */
fun KeyHolderContext.signTextEncrypted(
    text: String,
    encryptionKeyRing: PublicKeyRing = publicKeyRing,
    trimTrailingSpaces: Boolean = true
): EncryptedSignature =
    privateKeyRing.signTextEncrypted(
        text,
        encryptionKeyRing,
        trimTrailingSpaces
    )

/**
 * Sign [data] using [PrivateKeyRing]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [KeyHolderContext.verifyDataEncrypted]
 */
fun KeyHolderContext.signDataEncrypted(
    data: ByteArray,
    encryptionKeyRing: PublicKeyRing = publicKeyRing
): EncryptedSignature =
    privateKeyRing.signDataEncrypted(
        context,
        data,
        encryptionKeyRing
    )

/**
 * Sign [file] using [PrivateKeyRing]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [KeyHolderContext.verifyFileEncrypted]
 */
fun KeyHolderContext.signFileEncrypted(
    file: File,
    encryptionKeyRing: PublicKeyRing = publicKeyRing
): EncryptedSignature =
    privateKeyRing.signFileEncrypted(
        context,
        file,
        encryptionKeyRing
    )

/**
 * Decrypt [encryptedSignature] using [PrivateKeyRing]
 * and then verify it is a valid signature of [text] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 *
 * @see [KeyHolderContext.signTextEncrypted]
 */
fun KeyHolderContext.verifyTextEncrypted(
    text: String,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now,
    trimTrailingSpaces: Boolean = true
): Boolean = privateKeyRing.verifyTextEncrypted(
    text,
    encryptedSignature,
    verificationKeyRing,
    time,
    trimTrailingSpaces
)

/**
 * Decrypt [encryptedSignature] using [PrivateKeyRing]
 * and then verify it is a valid signature of [data] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [KeyHolderContext.signTextEncrypted]
 */
fun KeyHolderContext.verifyDataEncrypted(
    data: ByteArray,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = privateKeyRing.verifyDataEncrypted(
    context,
    data,
    encryptedSignature,
    verificationKeyRing,
    time
)

/**
 * Decrypt [encryptedSignature] using [PrivateKeyRing]
 * and then verify it is a valid signature of [file] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [KeyHolderContext.signTextEncrypted]
 */
fun KeyHolderContext.verifyFileEncrypted(
    file: File,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = privateKeyRing.verifyFileEncrypted(
    context,
    file,
    encryptedSignature,
    verificationKeyRing,
    time
)

/**
 * Verify [signature] of [text] is correctly signed using [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signText]
 */
fun KeyHolderContext.verifyText(
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    trimTrailingSpaces: Boolean = true
): Boolean = publicKeyRing.verifyText(context, text, signature, time, trimTrailingSpaces)

/**
 * Verify [signature] of [data] is correctly signed using [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signData]
 */
fun KeyHolderContext.verifyData(
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Boolean =
    publicKeyRing.verifyData(context, data, signature, time)

/**
 * Verify [signature] of [file] is correctly signed using [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signFile]
 */
fun KeyHolderContext.verifyFile(
    file: DecryptedFile,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Boolean =
    publicKeyRing.verifyFile(context, file, signature, time)

/**
 * Verify [signature] of [text] is correctly signed using [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 *
 * @return the timestamp if at least one [PublicKey] verify [signature], null otherwise
 *
 * @see [KeyHolderContext.signText]
 */
fun KeyHolderContext.getVerifiedTimestampOfText(
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    trimTrailingSpaces: Boolean = true
): Long? = publicKeyRing.getVerifiedTimestampOfText(context, text, signature, time, trimTrailingSpaces)

/**
 * Verify [signature] of [data] is correctly signed using [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return the timestamp if at least one [PublicKey] verify [signature], null otherwise
 *
 * @see [KeyHolderContext.signData]
 */
fun KeyHolderContext.getVerifiedTimestampOfData(
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Long? =
    publicKeyRing.getVerifiedTimestampOfData(context, data, signature, time)

/**
 * Encrypt [text] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptText]
 */
fun KeyHolderContext.encryptText(text: String): EncryptedMessage =
    publicKeyRing.encryptText(context, text)

/**
 * Encrypt [data] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun KeyHolderContext.encryptData(data: ByteArray): EncryptedMessage =
    publicKeyRing.encryptData(context, data)

/**
 * Encrypt [data] using [keyPacket].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun KeyHolderContext.encryptData(data: ByteArray, keyPacket: KeyPacket): DataPacket =
    decryptSessionKey(keyPacket).use { sessionKey -> sessionKey.encryptData(context, data) }

/**
 * Encrypt [data] using [sessionKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun KeyHolderContext.encryptData(data: ByteArray, sessionKey: SessionKey): DataPacket =
    sessionKey.encryptData(context, data)

/**
 * Encrypt [source] into [destination] using [keyPacket].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(source: File, destination: File, keyPacket: KeyPacket): EncryptedFile =
    decryptSessionKey(keyPacket).use { sessionKey -> sessionKey.encryptFile(context, source, destination) }

/**
 * Encrypt [source] into [destination] using [sessionKey].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(source: File, destination: File, sessionKey: SessionKey): EncryptedFile =
    sessionKey.encryptFile(context, source, destination)

/**
 * Encrypt [inputStream] using [PublicKeyRing].
 *
 * Note: Caller must delete generated [EncryptedFile] when needed.
 *
 * @throws [CryptoException] if [inputStream] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(fileName: String, inputStream: InputStream, keyPacket: KeyPacket): EncryptedFile {
    var source: File? = null
    var destination: File? = null
    try {
        source = File.createTempFile("$fileName.", "")
        destination = File.createTempFile("$fileName.", ".encrypted")
        inputStream.use { input -> source.outputStream().use { output -> input.copyTo(output) } }
        return encryptFile(source, destination, keyPacket)
    } catch (error: IOException) {
        destination?.delete()
        throw error
    } finally {
        source?.delete()
    }
}

/**
 * Encrypt [data] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(fileName: String, data: ByteArray, keyPacket: KeyPacket): EncryptedFile =
    encryptFile(fileName, ByteArrayInputStream(data), keyPacket)

/**
 * Encrypt [text] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt. Default: [KeyHolderContext.publicKeyRing].
 *
 * @return [EncryptedMessage] with embedded signature.
 *
 * @throws [CryptoException] if [text] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyText].
 */
fun KeyHolderContext.encryptAndSignText(
    text: String,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): EncryptedMessage =
    context.pgpCrypto.encryptAndSignText(
        text,
        encryptKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Compress and encrypt [text] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt. Default: [KeyHolderContext.publicKeyRing].
 *
 * @return [EncryptedMessage] with embedded signature.
 *
 * @throws [CryptoException] if [text] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyText].
 */
fun KeyHolderContext.encryptAndSignTextWithCompression(
    text: String,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): EncryptedMessage =
    context.pgpCrypto.encryptAndSignTextWithCompression(
        text,
        encryptKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Encrypt [data] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt. Default: [KeyHolderContext.publicKeyRing].
 *
 * @return [EncryptedMessage] with embedded signature.
 *
 * @throws [CryptoException] if [data] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyData].
 */
fun KeyHolderContext.encryptAndSignData(
    data: ByteArray,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): EncryptedMessage =
    context.pgpCrypto.encryptAndSignData(
        data,
        encryptKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Compress and encrypt [data] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt. Default: [KeyHolderContext.publicKeyRing].
 *
 * @return [EncryptedMessage] with embedded signature.
 *
 * @throws [CryptoException] if [data] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyData].
 */
fun KeyHolderContext.encryptAndSignDataWithCompression(
    data: ByteArray,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): EncryptedMessage =
    context.pgpCrypto.encryptAndSignDataWithCompression(
        data,
        encryptKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Encrypt [data] using [keyPacket] and sign using [KeyHolderContext.privateKeyRing].
 *
 * Note: If several operations use the same [keyPacket], prefer using [decryptSessionKey].
 *
 * @return [DataPacket] with embedded signature.
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyData]
 */
fun KeyHolderContext.encryptAndSignData(
    data: ByteArray,
    keyPacket: KeyPacket
): DataPacket = decryptSessionKey(keyPacket).use { sessionKey ->
    sessionKey.encryptAndSignData(context, data, privateKeyRing.unlockedPrimaryKey.unlockedKey.value)
}

/**
 * Encrypt [data] using [sessionKey] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @return [DataPacket] with embedded signature.
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyData]
 */
fun KeyHolderContext.encryptAndSignData(
    data: ByteArray,
    sessionKey: SessionKey
): DataPacket =
    sessionKey.encryptAndSignData(context, data, privateKeyRing.unlockedPrimaryKey.unlockedKey.value)

/**
 * Encrypt [source] into [destination] using [keyPacket] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @throws [CryptoException] if [source] cannot be encrypted or signed.
 *
 * @return [EncryptedFile] with embedded signature.
 *
 * If several operations use the same keyPacket, prefer `decryptSessionKey(keyPacket).use {...}`.
 *
 * @see [KeyHolderContext.decryptAndVerifyFile].
 */
fun KeyHolderContext.encryptAndSignFile(source: File, destination: File, keyPacket: KeyPacket): EncryptedFile =
    decryptSessionKey(keyPacket).use { sessionKey ->
        sessionKey.encryptAndSignFile(context, source, destination, privateKeyRing.unlockedPrimaryKey.unlockedKey.value)
    }

/**
 * Encrypt [source] into [destination] using [sessionKey] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @throws [CryptoException] if [source] cannot be encrypted or signed.
 *
 * @throws [CryptoException] if [source] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyFile].
 */
fun KeyHolderContext.encryptAndSignFile(source: File, destination: File, sessionKey: SessionKey): EncryptedFile =
    sessionKey.encryptAndSignFile(context, source, destination, privateKeyRing.unlockedPrimaryKey.unlockedKey.value)

/**
 * Encrypt [sessionKey] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [sessionKey] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptSessionKey]
 */
fun KeyHolderContext.encryptSessionKey(sessionKey: SessionKey): KeyPacket =
    publicKeyRing.encryptSessionKey(context, sessionKey)

/**
 * Encrypt [hashKey] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt. Default: [KeyHolderContext.publicKeyRing].
 *
 * @throws [CryptoException] if [hashKey] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyHashKey]
 */
fun KeyHolderContext.encryptAndSignHashKey(
    hashKey: HashKey,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): EncryptedMessage = encryptAndSignData(hashKey.key, encryptKeyRing)

/**
 * Decrypt [message] as [String] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignText]
 */
fun KeyHolderContext.decryptAndVerifyText(
    message: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedText =
    context.pgpCrypto.decryptAndVerifyText(
        message,
        verifyKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        time
    )

/**
 * Decrypt [message] as [ByteArray] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignData]
 */
fun KeyHolderContext.decryptAndVerifyData(
    message: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedData =
    context.pgpCrypto.decryptAndVerifyData(
        message,
        verifyKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        time
    )

/**
 * Decrypt and parse a PGP/MIME [message] using [KeyHolderContext.privateKeyRing].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyMimeMessage]
 */
fun KeyHolderContext.decryptMimeMessage(message: EncryptedMessage): DecryptedMimeMessage {
    return context.pgpCrypto.decryptMimeMessage(
        message,
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value }
    )
}

/**
 * Decrypt and parse a PGP/MIME [message] using [KeyHolderContext.privateKeyRing], and verify using [verifyKeyRing].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptMimeMessage]
 */
fun KeyHolderContext.decryptAndVerifyMimeMessage(
    message: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedMimeMessage {
    return context.pgpCrypto.decryptAndVerifyMimeMessage(
        message,
        verifyKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        time
    )
}

/**
 * Decrypt [data] as [ByteArray] using [keyPacket] and verify using [verifyKeyRing].
 *
 * Note: If several operations use the same [keyPacket], prefer using [decryptSessionKey].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [data] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptDataOrNull]
 * @see [KeyHolderContext.encryptData]
 */
fun KeyHolderContext.decryptAndVerifyData(
    data: DataPacket,
    keyPacket: KeyPacket,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedData = decryptSessionKey(keyPacket).use { sessionKey ->
    sessionKey.decryptAndVerifyData(context, data, verifyKeyRing.keys.map { it.key }, time)
}

/**
 * Decrypt [data] as [ByteArray] using [sessionKey] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [data] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptDataOrNull]
 * @see [KeyHolderContext.encryptData]
 */
fun KeyHolderContext.decryptAndVerifyData(
    data: DataPacket,
    sessionKey: SessionKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedData =
    sessionKey.decryptAndVerifyData(context, data, verifyKeyRing.keys.map { it.key }, time)

/**
 * Decrypt [source] into [destination] using [keyPacket] and verify using [verifyKeyRing].
 *
 * Note: If several operations use the same [keyPacket], prefer using [decryptSessionKey].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignFile]
 */
fun KeyHolderContext.decryptAndVerifyFile(
    source: EncryptedFile,
    destination: File,
    keyPacket: KeyPacket,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile = decryptSessionKey(keyPacket).use { sessionKey ->
    sessionKey.decryptAndVerifyFile(context, source, destination, verifyKeyRing.keys.map { it.key }, time)
}

/**
 * Decrypt [source] into [destination] using [sessionKey] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignFile]
 */
fun KeyHolderContext.decryptAndVerifyFile(
    source: EncryptedFile,
    destination: File,
    sessionKey: SessionKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile =
    sessionKey.decryptAndVerifyFile(context, source, destination, verifyKeyRing.keys.map { it.key }, time)

/**
 * Decrypt [message] as [String] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedText], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyText]
 */
fun KeyHolderContext.decryptAndVerifyTextOrNull(
    message: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedText? =
    context.pgpCrypto.decryptAndVerifyTextOrNull(
        message,
        verifyKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        time
    )

/**
 * Decrypt [message] as [ByteArray] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedData], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyData]
 */
fun KeyHolderContext.decryptAndVerifyDataOrNull(
    message: EncryptedMessage,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedData? =
    context.pgpCrypto.decryptAndVerifyDataOrNull(
        message,
        verifyKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        time
    )

/**
 * Decrypt [data] using [sessionKey] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify, default to [KeyHolderContext.publicKeyRing].
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedData], or `null` if [data] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyData]
 */
fun KeyHolderContext.decryptAndVerifyDataOrNull(
    data: DataPacket,
    sessionKey: SessionKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedData? =
    sessionKey.decryptAndVerifyDataOrNull(context, data, verifyKeyRing.keys.map { it.key }, time)

/**
 * Decrypt [source] into [destination] using [keyPacket] and verify using [verifyKeyRing].
 *
 * Note: If several operations use the same [keyPacket], prefer using [decryptSessionKey].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyFile]
 */
fun KeyHolderContext.decryptAndVerifyFileOrNull(
    source: EncryptedFile,
    destination: File,
    keyPacket: KeyPacket,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile? = decryptSessionKeyOrNull(keyPacket)?.use { sessionKey ->
    sessionKey.decryptAndVerifyFileOrNull(context, source, destination, verifyKeyRing.keys.map { it.key }, time)
}

/**
 * Decrypt [source] into [destination] using [sessionKey] and verify using [verifyKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyFile]
 */
fun KeyHolderContext.decryptAndVerifyFileOrNull(
    source: EncryptedFile,
    destination: File,
    sessionKey: SessionKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile? =
    sessionKey.decryptAndVerifyFileOrNull(context, source, destination, verifyKeyRing.keys.map { it.key }, time)

/**
 * Get [Armored] from [Unarmored].
 *
 * Note: By default, use [PGPHeader.Message].
 *
 * @throws [CryptoException] if data cannot be extracted.
 */
fun KeyHolderContext.getArmored(data: Unarmored, header: PGPHeader = PGPHeader.Message): Armored =
    context.pgpCrypto.getArmored(data, header)

/**
 * Get [Unarmored] from [Armored].
 *
 * @throws [CryptoException] if data cannot be extracted.
 */
fun KeyHolderContext.getUnarmored(data: Armored): Unarmored =
    context.pgpCrypto.getUnarmored(data)

/**
 * Get list of [EncryptedPacket] from [message].
 *
 * @throws [CryptoException] if key and data packets cannot be extracted from [message].
 */
fun KeyHolderContext.getEncryptedPackets(message: EncryptedMessage): List<EncryptedPacket> =
    context.pgpCrypto.getEncryptedPackets(message)

/**
 * Decrypt [nestedPrivateKey]'s passphrase using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 *
 * @param validTokenPredicate a predicate function to specify
 * what format should a valid token have. Default: always true predicate.
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 *
 * @throws [IllegalStateException] if there is no encrypted passphrase or signature.
 * If the passphrase cannot be decrypted or verified.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKeyOrThrow(
    nestedPrivateKey: NestedPrivateKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    validTokenPredicate: (ByteArray) -> Boolean = { true }
): NestedPrivateKey {
    checkNotNull(nestedPrivateKey.passphrase) { "Cannot decrypt key without encrypted passphrase." }
    checkNotNull(nestedPrivateKey.passphraseSignature) { "Cannot verify without passphrase signature." }
    val passphrase = decryptData(nestedPrivateKey.passphrase).use { decrypted ->
        check(verifyKeyRing.verifyData(context, decrypted.array, nestedPrivateKey.passphraseSignature)) {
            "Cannot verify key passphrase using provided signature."
        }
        check(validTokenPredicate(decrypted.array)) {
            "Passphrase doesn't have the expected format"
        }
        decrypted.encrypt(context.keyStoreCrypto)
    }
    return nestedPrivateKey.copy(
        privateKey = nestedPrivateKey.privateKey.copy(
            isActive = true,
            passphrase = passphrase
        )
    )
}


/**
 * Decrypt [passphrase] using [KeyHolderContext.privateKeyRing]] and verify [signature] using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify the signature. Default: [KeyHolderContext.publicKeyRing].
 *
 * @param validTokenPredicate a predicate function to specify
 * what format should a valid token have. Default: always true predicate.
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 *
 * @throws [IllegalStateException] if there is no encrypted passphrase or signature.
 * If the passphrase cannot be decrypted or verified.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKeyOrThrow(
    key: Armored,
    passphrase: EncryptedMessage,
    signature: Signature,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    validTokenPredicate: (ByteArray) -> Boolean = { true }
): NestedPrivateKey = decryptAndVerifyNestedKeyOrThrow(
    NestedPrivateKey.from(key, passphrase, signature),
    verifyKeyRing,
    validTokenPredicate
)

/**
 * Decrypt [nestedPrivateKey] using [KeyHolderContext.privateKeyRing] and verify using [verifyKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify. Default: [KeyHolderContext.publicKeyRing].
 *
 * @param validTokenPredicate a predicate function to specify
 * what format should a valid token have. Default: always true predicate.
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 * Or null if its passphrase cannot be decrypted or verified.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKeyOrNull(
    nestedPrivateKey: NestedPrivateKey,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    validTokenPredicate: (ByteArray) -> Boolean = { true }
): NestedPrivateKey? = runCatching {
    decryptAndVerifyNestedKeyOrThrow(nestedPrivateKey, verifyKeyRing, validTokenPredicate)
}.onFailure {
    CoreLogger.d(LogTag.DEFAULT, it, "Cannot decrypt and/or verify nested key.")
}.getOrNull()

/**
 * Decrypt [passphrase] using [PrivateKeyRing] and verify [signature] using [PublicKeyRing].
 *
 * @param verifyKeyRing [PublicKeyRing] used to verify the signature. Default: [KeyHolderContext.publicKeyRing].
 *
 * @param validTokenPredicate a predicate function to specify
 * what format should a valid token have. Default: always true predicate.
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 * Or null if its passphrase cannot be decrypted or verified.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKeyOrNull(
    key: Armored,
    passphrase: EncryptedMessage,
    signature: Signature,
    verifyKeyRing: PublicKeyRing = publicKeyRing,
    validTokenPredicate: (ByteArray) -> Boolean = { true }
): NestedPrivateKey? = runCatching {
    decryptAndVerifyNestedKeyOrThrow(key, passphrase, signature, verifyKeyRing, validTokenPredicate)
}.onFailure {
    CoreLogger.d(LogTag.DEFAULT, it, "Cannot decrypt and/or verify nested key.")
}.getOrNull()

/**
 * Encrypt [PrivateKey.passphrase] using [encryptKeyRing] and sign using [KeyHolderContext.privateKeyRing].
 *
 * @param encryptKeyRing [PublicKeyRing] used to encrypt passphrase. Default: [KeyHolderContext.publicKeyRing].
 *
 * @throws [CryptoException] if [nestedPrivateKey] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyData].
 */
fun KeyHolderContext.encryptAndSignNestedKey(
    nestedPrivateKey: NestedPrivateKey,
    encryptKeyRing: PublicKeyRing = publicKeyRing
): NestedPrivateKey {
    checkNotNull(nestedPrivateKey.privateKey.passphrase) { "Cannot encrypt without passphrase." }
    return nestedPrivateKey.privateKey.passphrase.decrypt(context.keyStoreCrypto).use { passphrase ->
        nestedPrivateKey.copy(
            privateKey = nestedPrivateKey.privateKey.copy(isActive = false, passphrase = null),
            passphrase = encryptKeyRing.encryptData(context, passphrase.array),
            passphraseSignature = signData(passphrase.array)
        )
    }
}

/**
 * Generate new [NestedPrivateKey].
 *
 * Note: Only this [KeyHolder] will be able to decrypt.
 */
fun KeyHolderContext.generateNestedPrivateKey(
    username: String,
    domain: String,
    generatePassphrase: () -> ByteArray = { context.pgpCrypto.generateNewToken() }
): NestedPrivateKey =
    NestedPrivateKey.generateNestedPrivateKey(context, username, domain, generatePassphrase)

/**
 * Generate new [SessionKey].
 *
 * Note: Consider using [use] on returned [SessionKey], to clear memory after usage.
 *
 * @see generateNewKeyPacket
 */
fun KeyHolderContext.generateNewSessionKey(): SessionKey = context.pgpCrypto.generateNewSessionKey()

/**
 * Generate new [KeyPacket].
 *
 * @see generateNewSessionKey
 */
fun KeyHolderContext.generateNewKeyPacket(): KeyPacket = generateNewSessionKey().use { encryptSessionKey(it) }

/**
 * Generate new [HashKey].
 *
 * Note: Consider using [use] on returned [SessionKey], to clear memory after usage.
 */
fun KeyHolderContext.generateNewHashKey(): HashKey = context.pgpCrypto.generateNewHashKey()

/**
 * Get Base64 encoded string from [array].
 *
 * @see getBase64Decoded
 */
fun KeyHolderContext.getBase64Encoded(array: ByteArray): EncryptedMessage =
    context.pgpCrypto.getBase64Encoded(array)

/**
 * Get Base64 decoded array from [string].
 *
 * @see getBase64Encoded
 */
fun KeyHolderContext.getBase64Decoded(string: String): ByteArray =
    context.pgpCrypto.getBase64Decoded(string)
