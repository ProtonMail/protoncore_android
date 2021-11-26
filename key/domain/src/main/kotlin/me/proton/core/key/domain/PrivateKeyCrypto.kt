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
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.pgp.unlockOrNull
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import java.io.File

/**
 * Get fingerprint from this [PrivateKey].
 *
 * @throws [CryptoException] if fingerprint cannot be extracted.
 */
fun PrivateKey.fingerprint(context: CryptoContext) =
    context.pgpCrypto.getFingerprint(key)

/**
 * Get [PublicKey] from [PrivateKey].
 *
 * @throws [CryptoException] if public key cannot be extracted.
 */
fun PrivateKey.publicKey(context: CryptoContext): PublicKey =
    context.pgpCrypto.getPublicKey(key).let {
        PublicKey(
            key = it,
            isPrimary = isPrimary,
            isActive = isActive,
            canEncrypt = canEncrypt,
            canVerify = canVerify
        )
    }

/**
 * Encrypt [text] using this [PublicKey].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [PublicKey.encryptData]
 */
fun PrivateKey.encryptText(context: CryptoContext, text: String): EncryptedMessage =
    publicKey(context).encryptText(context, text)

/**
 * Encrypt [data] using this [PublicKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [PrivateKey.encryptText]
 */
fun PrivateKey.encryptData(context: CryptoContext, data: ByteArray): EncryptedMessage =
    publicKey(context).encryptData(context, data)

/**
 * Decrypt [message] as [String] using this [PrivateKey].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKey.decryptTextOrNull]
 */
fun PrivateKey.decryptText(context: CryptoContext, message: EncryptedMessage): String =
    unlock(context).use { it.decryptText(context, message) }

/**
 * Decrypt [message] as [ByteArray] using this [PrivateKey].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKey.decryptDataOrNull]
 */
fun PrivateKey.decryptData(context: CryptoContext, message: EncryptedMessage): ByteArray =
    unlock(context).use { it.decryptData(context, message) }

/**
 * Decrypt [message] as [String] using this [PrivateKey].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKey.decryptText]
 */
fun PrivateKey.decryptTextOrNull(context: CryptoContext, message: EncryptedMessage): String? =
    unlock(context).use { it.decryptTextOrNull(context, message) }

/**
 * Decrypt [message] as [ByteArray] using this [PrivateKey].
 *
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKey.decryptData]
 */
fun PrivateKey.decryptDataOrNull(context: CryptoContext, message: EncryptedMessage): ByteArray? =
    unlock(context).use { it.decryptDataOrNull(context, message) }

/**
 * Sign [text] using this [PrivateKey].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [PublicKey.verifyText]
 */
fun PrivateKey.signText(context: CryptoContext, text: String): Signature =
    unlock(context).use { it.signText(context, text) }

/**
 * Sign [data] using this [PrivateKey].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [PublicKey.verifyData]
 */
fun PrivateKey.signData(context: CryptoContext, data: ByteArray): Signature =
    unlock(context).use { it.signData(context, data) }

/**
 * Unlock this [PrivateKey] using embedded passphrase.
 *
 * @return [UnlockedPrivateKey] implementing Closeable to clear memory after usage.
 *
 * @throws [CryptoException] if [PrivateKey] cannot be unlocked.
 *
 * @see [PrivateKey.unlockOrNull]
 * @see [UnlockedPrivateKey.lock]
 */
fun PrivateKey.unlock(context: CryptoContext): UnlockedPrivateKey =
    requireNotNull(passphrase).decrypt(context.keyStoreCrypto).use { decrypted ->
        context.pgpCrypto.unlock(key, decrypted.array).let {
            UnlockedPrivateKey(it, isPrimary)
        }
    }

/**
 * Unlock this [PrivateKey] using embedded passphrase.
 *
 * @return [UnlockedPrivateKey], or `null` if [PrivateKey] cannot be unlocked.
 *
 * @see [PrivateKey.unlock]
 * @see [UnlockedPrivateKey.lock]
 */
fun PrivateKey.unlockOrNull(context: CryptoContext): UnlockedPrivateKey? =
    passphrase?.decrypt(context.keyStoreCrypto)?.use { decrypted ->
        context.pgpCrypto.unlockOrNull(key, decrypted.array)?.let {
            UnlockedPrivateKey(it, isPrimary)
        }
    }

/**
 * @return true if this [PrivateKey] can be unlocked using embedded passphrase.
 *
 * @see [PrivateKey.unlock]
 * @see [PrivateKey.unlockOrNull]
 */
fun PrivateKey.canUnlock(context: CryptoContext): Boolean =
    unlockOrNull(context) != null

/**
 * @return true if this [PrivateKey] can be unlocked using provided [passphrase].
 *
 * @see [PrivateKey.unlock]
 * @see [PrivateKey.unlockOrNull]
 */
fun PrivateKey.canUnlock(context: CryptoContext, passphrase: EncryptedByteArray?): Boolean =
    copy(passphrase = passphrase).canUnlock(context)

/**
 * Decrypt [message] as [String] using this [PrivateKeyRing.keys].
 *
 * Note: String canonicalization/standardization is applied.
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptTextOrNull]
 * @see [PublicKeyRing.encryptText]
 */
fun PrivateKeyRing.decryptText(message: EncryptedMessage): String =
    unlockedKeys.decryptText(context, message)

/**
 * Decrypt [message] as [ByteArray] using this [PrivateKeyRing.keys].
 *
 * @throws [CryptoException] if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptDataOrNull]
 * @see [PublicKeyRing.encryptData]
 */
fun PrivateKeyRing.decryptData(message: EncryptedMessage): ByteArray =
    unlockedKeys.decryptData(context, message)

/**
 * Decrypt [keyPacket] as [SessionKey] using this [PrivateKeyRing.keys].
 *
 * @throws [CryptoException] if [keyPacket] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptSessionKeyOrNull]
 * @see [PublicKeyRing.encryptSessionKey]
 */
fun PrivateKeyRing.decryptSessionKey(keyPacket: KeyPacket): SessionKey =
    unlockedKeys.decryptSessionKey(context, keyPacket)

/**
 * Decrypt [message] as [String] using this [PrivateKeyRing.keys].
 *
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptText]
 */
fun PrivateKeyRing.decryptTextOrNull(message: EncryptedMessage): String? =
    unlockedKeys.decryptTextOrNull(context, message)

/**
 * Decrypt [message] as [ByteArray] using this [PrivateKeyRing.keys].
 *
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptData]
 */
fun PrivateKeyRing.decryptDataOrNull(message: EncryptedMessage): ByteArray? =
    unlockedKeys.decryptDataOrNull(context, message)

/**
 * Decrypt [keyPacket] as [SessionKey] using this [PrivateKeyRing.keys].
 *
 * @return [SessionKey], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [PrivateKeyRing.decryptSessionKey]
 */
fun PrivateKeyRing.decryptSessionKeyOrNull(keyPacket: KeyPacket): SessionKey? =
    unlockedKeys.decryptSessionKeyOrNull(context, keyPacket)

/**
 * Sign [text] using primary [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [PublicKeyRing.verifyText]
 */
fun PrivateKeyRing.signText(text: String): Signature =
    unlockedPrimaryKey.signText(context, text)

/**
 * Sign [data] using primary [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [PublicKeyRing.verifyData]
 */
fun PrivateKeyRing.signData(data: ByteArray): Signature =
    unlockedPrimaryKey.signData(context, data)

/**
 * Sign [file] using primary [UnlockedPrivateKey].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [PublicKeyRing.verifyFile]
 */
fun PrivateKeyRing.signFile(file: File): Signature =
    unlockedPrimaryKey.signFile(context, file)

/**
 * Sign [text] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [text] cannot be signed.
 *
 * @see [PrivateKeyRing.verifyTextEncrypted]
 */
fun PrivateKeyRing.signTextEncrypted(
    context: CryptoContext,
    text: String,
    encryptionKeyRing: PublicKeyRing
): Signature =
    unlockedPrimaryKey.signTextEncrypted(context, text, encryptionKeyRing)

/**
 * Sign [data] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [data] cannot be signed.
 *
 * @see [PrivateKeyRing.verifyDataEncrypted]
 */
fun PrivateKeyRing.signDataEncrypted(
    context: CryptoContext,
    data: ByteArray,
    encryptionKeyRing: PublicKeyRing
): Signature =
    unlockedPrimaryKey.signDataEncrypted(context, data, encryptionKeyRing)

/**
 * Sign [file] using this [UnlockedPrivateKey]
 * and then encrypt the signature with [encryptionKeyRing].
 *
 * @throws [CryptoException] if [file] cannot be signed.
 *
 * @see [PrivateKeyRing.verifyFileEncrypted]
 */
fun PrivateKeyRing.signFileEncrypted(
    context: CryptoContext,
    file: File,
    encryptionKeyRing: PublicKeyRing
): Signature =
    unlockedPrimaryKey.signFileEncrypted(context, file, encryptionKeyRing)

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [text] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signTextEncrypted]
 */
fun PrivateKeyRing.verifyTextEncrypted(
    context: CryptoContext,
    text: String,
    encryptedSignature: Armored,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = unlockedPrimaryKey.verifyTextEncrypted(
    context,
    text,
    encryptedSignature,
    verificationKeyRing,
    time
)

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [data] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signTextEncrypted]
 */
fun PrivateKeyRing.verifyDataEncrypted(
    context: CryptoContext,
    data: ByteArray,
    encryptedSignature: Armored,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = unlockedPrimaryKey.verifyDataEncrypted(
    context,
    data,
    encryptedSignature,
    verificationKeyRing,
    time
)

/**
 * Decrypt [encryptedSignature] using this [UnlockedPrivateKey]
 * and then verify it is a valid signature of [file] using [verificationKeyRing]
 *
 * @param time time for [encryptedSignature] validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signTextEncrypted]
 */
fun PrivateKeyRing.verifyFileEncrypted(
    context: CryptoContext,
    file: File,
    encryptedSignature: Armored,
    verificationKeyRing: PublicKeyRing,
    time: VerificationTime = VerificationTime.Now
): Boolean = unlockedPrimaryKey.verifyFileEncrypted(
    context,
    file,
    encryptedSignature,
    verificationKeyRing,
    time
)
