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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import me.proton.core.crypto.common.pgp.decryptSessionKeyOrNull
import me.proton.core.crypto.common.pgp.decryptTextOrNull
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import java.io.File

/**
 * Decrypt [message] as [String].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptText]
 */
fun List<UnlockedPrivateKey>.decryptText(context: CryptoContext, message: EncryptedMessage): String {
    forEach { it.decryptTextOrNull(context, message)?.let { decrypted -> return decrypted } }
    throw CryptoException("Cannot decrypt message with provided Key list.")
}

/**
 * Decrypt [message] as [ByteArray].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptData]
 */
fun List<UnlockedPrivateKey>.decryptData(context: CryptoContext, message: EncryptedMessage): ByteArray {
    forEach { it.decryptDataOrNull(context, message)?.let { decrypted -> return decrypted } }
    throw CryptoException("Cannot decrypt message with provided Key list.")
}

/**
 * Decrypt [keyPacket] as [SessionKey].
 *
 * @throws [CryptoException] if [keyPacket] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptData]
 */
fun List<UnlockedPrivateKey>.decryptSessionKey(context: CryptoContext, keyPacket: KeyPacket): SessionKey {
    forEach { it.decryptSessionKeyOrNull(context, keyPacket)?.let { decrypted -> return decrypted } }
    throw CryptoException("Cannot decrypt keyPacket with provided Key list.")
}

/**
 * Decrypt [message] as [String].
 *
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptText]
 */
fun List<UnlockedPrivateKey>.decryptTextOrNull(context: CryptoContext, message: EncryptedMessage): String? {
    forEach { it.decryptTextOrNull(context, message)?.let { decrypted -> return decrypted } }
    return null
}

/**
 * Decrypt [message] as [ByteArray].
 *
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptData]
 */
fun List<UnlockedPrivateKey>.decryptDataOrNull(context: CryptoContext, message: EncryptedMessage): ByteArray? {
    forEach { it.decryptDataOrNull(context, message)?.let { decrypted -> return decrypted } }
    return null
}

/**
 * Decrypt [keyPacket] as [SessionKey].
 *
 * @return [ByteArray], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptSessionKey]
 */
fun List<UnlockedPrivateKey>.decryptSessionKeyOrNull(context: CryptoContext, keyPacket: KeyPacket): SessionKey? {
    forEach { it.decryptSessionKeyOrNull(context, keyPacket)?.let { decrypted -> return decrypted } }
    return null
}

/**
 * Decrypt [message] as [String].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptTextOrNull]
 */
fun UnlockedPrivateKey.decryptText(context: CryptoContext, message: EncryptedMessage): String =
    context.pgpCrypto.decryptText(message, unlockedKey.value)

/**
 * Decrypt [message] as [ByteArray].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptDataOrNull]
 */
fun UnlockedPrivateKey.decryptData(context: CryptoContext, message: EncryptedMessage): ByteArray =
    context.pgpCrypto.decryptData(message, unlockedKey.value)

/**
 * Decrypt [keyPacket] as [SessionKey].
 *
 * @throws [CryptoException] if [keyPacket] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptSessionKeyOrNull]
 */
fun UnlockedPrivateKey.decryptSessionKey(context: CryptoContext, keyPacket: KeyPacket): SessionKey =
    context.pgpCrypto.decryptSessionKey(keyPacket, unlockedKey.value)

/**
 * Decrypt [message] as [String].
 *
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun UnlockedPrivateKey.decryptTextOrNull(context: CryptoContext, message: EncryptedMessage): String? =
    context.pgpCrypto.decryptTextOrNull(message, unlockedKey.value)

/**
 * Decrypt [message] as [ByteArray].
 *
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptData]
 */
fun UnlockedPrivateKey.decryptDataOrNull(context: CryptoContext, message: EncryptedMessage): ByteArray? =
    context.pgpCrypto.decryptDataOrNull(message, unlockedKey.value)

/**
 * Decrypt [keyPacket] as [SessionKey].
 *
 * @return [SessionKey], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [UnlockedPrivateKey.decryptData]
 */
fun UnlockedPrivateKey.decryptSessionKeyOrNull(context: CryptoContext, keyPacket: KeyPacket): SessionKey? =
    context.pgpCrypto.decryptSessionKeyOrNull(keyPacket, unlockedKey.value)

/**
 * Sign [text] using this [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [PublicKey.verifyText]
 */
fun UnlockedPrivateKey.signText(context: CryptoContext, text: String): Signature =
    context.pgpCrypto.signText(text, unlockedKey.value)

/**
 * Sign [data] using this [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [PublicKey.verifyData]
 */
fun UnlockedPrivateKey.signData(context: CryptoContext, data: ByteArray): Signature =
    context.pgpCrypto.signData(data, unlockedKey.value)

/**
 * Sign [file] using this [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [PublicKey.verifyFile]
 */
fun UnlockedPrivateKey.signFile(context: CryptoContext, file: File): Signature =
    context.pgpCrypto.signFile(file, unlockedKey.value)

/**
 * Sign [text] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [UnlockedPrivateKey.verifyTextEncrypted]
 */
fun UnlockedPrivateKey.signTextEncrypted(
    context: CryptoContext,
    text: String,
    encryptionKeyRing: PublicKeyRing
): EncryptedSignature =
    context.pgpCrypto.signTextEncrypted(text, unlockedKey.value, encryptionKeyRing.keys.map { it.key })

/**
 * Sign [data] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [UnlockedPrivateKey.verifyDataEncrypted]
 */
fun UnlockedPrivateKey.signDataEncrypted(
    context: CryptoContext,
    data: ByteArray,
    encryptionKeyRing: PublicKeyRing
): EncryptedSignature =
    context.pgpCrypto.signDataEncrypted(data, unlockedKey.value, encryptionKeyRing.keys.map { it.key })

/**
 * Sign [file] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [UnlockedPrivateKey.verifyFileEncrypted]
 */
fun UnlockedPrivateKey.signFileEncrypted(
    context: CryptoContext,
    file: File,
    encryptionKeyRing: PublicKeyRing
): EncryptedSignature =
    context.pgpCrypto.signFileEncrypted(file, unlockedKey.value, encryptionKeyRing.keys.map { it.key })

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [text] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [UnlockedPrivateKey.signTextEncrypted]
 */
fun UnlockedPrivateKey.verifyTextEncrypted(
    context: CryptoContext,
    text: String,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = context.pgpCrypto.verifyTextEncrypted(
        text,
        encryptedSignature,
        unlockedKey.value,
        verificationKeyRing.keys.map { it.key },
        time
    )

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [data] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [UnlockedPrivateKey.signTextEncrypted]
 */
fun UnlockedPrivateKey.verifyDataEncrypted(
    context: CryptoContext,
    data: ByteArray,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = context.pgpCrypto.verifyDataEncrypted(
    data,
    encryptedSignature,
    unlockedKey.value,
    verificationKeyRing.keys.map { it.key },
    time
)

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [file] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [UnlockedPrivateKey.signTextEncrypted]
 */
fun UnlockedPrivateKey.verifyFileEncrypted(
    context: CryptoContext,
    file: File,
    encryptedSignature: EncryptedSignature,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = context.pgpCrypto.verifyFileEncrypted(
    file,
    encryptedSignature,
    unlockedKey.value,
    verificationKeyRing.keys.map { it.key },
    time
)

/**
 * Lock this [UnlockedPrivateKey] using [passphrase].
 *
 * @throws [CryptoException] if [UnlockedPrivateKey] cannot be locked using [passphrase].
 *
 * @see [PrivateKey.unlock]
 */
fun UnlockedPrivateKey.lock(
    context: CryptoContext,
    passphrase: EncryptedByteArray,
    isPrimary: Boolean = true,
    isActive: Boolean = true,
    canEncrypt: Boolean = true,
    canVerify: Boolean = true,
): PrivateKey = passphrase.decrypt(context.keyStoreCrypto).use { decrypted ->
    context.pgpCrypto.lock(unlockedKey.value, decrypted.array).let {
        PrivateKey(
            key = it,
            isPrimary = isPrimary,
            isActive = isActive,
            canEncrypt = canEncrypt,
            canVerify = canVerify,
            passphrase = passphrase
        )
    }
}
