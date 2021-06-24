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
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.decryptAndVerifyDataOrNull
import me.proton.core.crypto.common.pgp.decryptAndVerifyTextOrNull
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
 */
fun <R> KeyHolder.useKeys(context: CryptoContext, block: KeyHolderContext.() -> R): R {
    val privateKeys = keys.map { key -> key.privateKey }
    val publicKeys = privateKeys.map { key -> key.publicKey(context) }
    val keyHolderContext = KeyHolderContext(
        context = context,
        privateKeyRing = PrivateKeyRing(context, privateKeys),
        publicKeyRing = PublicKeyRing(publicKeys)
    )
    return keyHolderContext.use { block(it) }
}

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
 * Decrypt [source] into [destination] as [DecryptedFile] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptFileOrNull]
 * @see [KeyHolderContext.encryptFile]
 */
fun KeyHolderContext.decryptFile(source: EncryptedFile, destination: File): DecryptedFile =
    privateKeyRing.decryptFile(source, destination)

/**
 * Decrypt [keyPacket] as [ByteArray] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [keyPacket] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptSessionKeyOrNull]
 * @see [KeyHolderContext.encryptSessionKey]
 */
fun KeyHolderContext.decryptSessionKey(keyPacket: KeyPacket): ByteArray =
    privateKeyRing.decryptSessionKey(keyPacket)

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
 * Decrypt [source] into [destination] as [EncryptedFile] using [PrivateKeyRing].
 *
 * @return [EncryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.decryptFileOrNull(source: EncryptedFile, destination: File): DecryptedFile? =
    privateKeyRing.decryptFileOrNull(source, destination)

/**
 * Decrypt [keyPacket] as [ByteArray] using [PrivateKeyRing].
 *
 * @return [ByteArray], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptSessionKey]
 */
fun KeyHolderContext.decryptSessionKeyOrNull(keyPacket: KeyPacket): ByteArray? =
    privateKeyRing.decryptSessionKeyOrNull(keyPacket)

/**
 * Sign [text] using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [KeyHolderContext.verifyText]
 */
fun KeyHolderContext.signText(text: String): Signature =
    privateKeyRing.signText(text)

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
 * Verify [signature] of [text] is correctly signed using [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signText]
 */
fun KeyHolderContext.verifyText(text: String, signature: Signature, validAtUtc: Long = 0): Boolean =
    publicKeyRing.verifyText(context, text, signature, validAtUtc)

/**
 * Verify [signature] of [data] is correctly signed using [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signData]
 */
fun KeyHolderContext.verifyData(data: ByteArray, signature: Signature, validAtUtc: Long = 0): Boolean =
    publicKeyRing.verifyData(context, data, signature, validAtUtc)

/**
 * Verify [signature] of [file] is correctly signed using [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [KeyHolderContext.signFile]
 */
fun KeyHolderContext.verifyFile(file: DecryptedFile, signature: Signature, validAtUtc: Long = 0): Boolean =
    publicKeyRing.verifyFile(context, file, signature, validAtUtc)

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
 * Encrypt [source] into [destination] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(source: File, destination: File): EncryptedFile =
    publicKeyRing.encryptFile(context, source, destination)

/**
 * Encrypt [inputStream] using [PublicKeyRing].
 *
 * Note: Caller must delete generated [EncryptedFile.file] when needed.
 *
 * @throws [CryptoException] if [inputStream] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptFile]
 */
fun KeyHolderContext.encryptFile(fileName: String, inputStream: InputStream): EncryptedFile {
    var source: File? = null
    var destination: File? = null
    try {
        source = File.createTempFile("$fileName.", "")
        destination = File.createTempFile("$fileName.", ".encrypted")
        inputStream.use { input -> source.outputStream().use { output -> input.copyTo(output) } }
        return encryptFile(source, destination)
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
fun KeyHolderContext.encryptFile(fileName: String, data: ByteArray): EncryptedFile =
    encryptFile(fileName, ByteArrayInputStream(data))

/**
 * Encrypt [text] using [PublicKeyRing] and sign using [PrivateKeyRing] in an embedded [EncryptedMessage].
 *
 * @throws [CryptoException] if [text] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyText].
 */
fun KeyHolderContext.encryptAndSignText(text: String): EncryptedMessage =
    context.pgpCrypto.encryptAndSignText(
        text,
        publicKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Encrypt [data] using [PublicKeyRing] and sign using [PrivateKeyRing] in an embedded [EncryptedMessage].
 *
 * @throws [CryptoException] if [data] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyData].
 */
fun KeyHolderContext.encryptAndSignData(data: ByteArray): EncryptedMessage =
    context.pgpCrypto.encryptAndSignData(
        data,
        publicKeyRing.primaryKey.key,
        privateKeyRing.unlockedPrimaryKey.unlockedKey.value
    )

/**
 * Encrypt [keyPacket] using [PublicKeyRing].
 *
 * @throws [CryptoException] if [keyPacket] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptSessionKey]
 */
fun KeyHolderContext.encryptSessionKey(keyPacket: KeyPacket): ByteArray =
    publicKeyRing.encryptSessionKey(context, keyPacket)

/**
 * Decrypt [message] as [String] using [PrivateKeyRing] and verify using [PublicKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @param validAtUtc UTC time for embedded signature validation, or 0 to ignore time.
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignText]
 */
fun KeyHolderContext.decryptAndVerifyText(message: EncryptedMessage, validAtUtc: Long = 0): DecryptedText =
    context.pgpCrypto.decryptAndVerifyText(
        message,
        publicKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        validAtUtc
    )

/**
 * Decrypt [message] as [ByteArray] using [PrivateKeyRing] and verify using [PublicKeyRing].
 *
 * @param validAtUtc UTC time for embedded signature validation, or 0 to ignore time.
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.encryptAndSignData]
 */
fun KeyHolderContext.decryptAndVerifyData(message: EncryptedMessage, validAtUtc: Long = 0): DecryptedData =
    context.pgpCrypto.decryptAndVerifyData(
        message,
        publicKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        validAtUtc
    )

/**
 * Decrypt [message] as [String] using [PrivateKeyRing] and verify using [PublicKeyRing].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @param validAtUtc UTC time for embedded signature validation, or 0 to ignore time.
 *
 * @return [DecryptedText], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyText]
 */
fun KeyHolderContext.decryptAndVerifyTextOrNull(message: EncryptedMessage, validAtUtc: Long = 0): DecryptedText? =
    context.pgpCrypto.decryptAndVerifyTextOrNull(
        message,
        publicKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        validAtUtc
    )

/**
 * Decrypt [message] as [ByteArray] using [PrivateKeyRing] and verify using [PublicKeyRing].
 *
 * @param validAtUtc UTC time for embedded signature validation, or 0 to ignore time.
 *
 * @return [DecryptedData], or `null` if [message] cannot be decrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyData]
 */
fun KeyHolderContext.decryptAndVerifyDataOrNull(message: EncryptedMessage, validAtUtc: Long = 0): DecryptedData? =
    context.pgpCrypto.decryptAndVerifyDataOrNull(
        message,
        publicKeyRing.keys.map { it.key },
        privateKeyRing.unlockedKeys.map { it.unlockedKey.value },
        validAtUtc
    )

/**
 * Get [Armored] from [Unarmored].
 *
 * @throws [CryptoException] if data cannot be extracted.
 */
fun KeyHolderContext.getArmored(data: Unarmored): Armored =
    context.pgpCrypto.getArmored(data)

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
 * Decrypt [nestedPrivateKey] using [PrivateKeyRing] and verify using [PublicKeyRing].
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 *
 * @throws [IllegalStateException] if there is no encrypted or signed data.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKey(nestedPrivateKey: NestedPrivateKey): NestedPrivateKey {
    checkNotNull(nestedPrivateKey.passphrase) { "Cannot decrypt key without encrypted passphrase." }
    checkNotNull(nestedPrivateKey.passphraseSignature) { "Cannot verify without passphrase signature." }
    return nestedPrivateKey.copy(
        privateKey = nestedPrivateKey.privateKey.copy(
            passphrase = decryptDataOrNull(nestedPrivateKey.passphrase)
                ?.takeIf { verifyData(it, nestedPrivateKey.passphraseSignature) }
                ?.let { plain -> plain.use { it.encryptWith(context.keyStoreCrypto) } }
        )
    )
}

/**
 * Decrypt [passphrase] using [PrivateKeyRing] and verify [signature] using [PublicKeyRing].
 *
 * @return [NestedPrivateKey] with ready to use [PrivateKey].
 *
 * @throws [IllegalStateException] if there is no encrypted or signed data.
 *
 * @see [KeyHolderContext.encryptAndSignNestedKey]
 */
fun KeyHolderContext.decryptAndVerifyNestedKey(
    key: Armored,
    passphrase: EncryptedMessage,
    signature: Signature
): NestedPrivateKey =
    decryptAndVerifyNestedKey(NestedPrivateKey.from(key = key, passphrase = passphrase, signature = signature))

/**
 * Encrypt [PrivateKey.passphrase] using [PublicKeyRing] and sign using [PrivateKeyRing].
 *
 * @throws [CryptoException] if [nestedPrivateKey] cannot be encrypted or signed.
 *
 * @see [KeyHolderContext.decryptAndVerifyData].
 */
fun KeyHolderContext.encryptAndSignNestedKey(nestedPrivateKey: NestedPrivateKey): NestedPrivateKey {
    checkNotNull(nestedPrivateKey.privateKey.passphrase) { "Cannot encrypt without passphrase." }
    return nestedPrivateKey.privateKey.passphrase.decryptWith(context.keyStoreCrypto).use { passphrase ->
        nestedPrivateKey.copy(
            privateKey = nestedPrivateKey.privateKey.copy(passphrase = null),
            passphrase = encryptData(passphrase.array),
            passphraseSignature = signData(passphrase.array)
        )
    }
}

/**
 * Generate and encrypt a new [NestedPrivateKey] from [KeyHolder] keys.
 *
 * Note: Only this [KeyHolder] will be able to decrypt.
 */
fun KeyHolderContext.generateNestedPrivateKey(username: String, domain: String): NestedPrivateKey =
    encryptAndSignNestedKey(NestedPrivateKey.generateNestedPrivateKey(context, username, domain))
