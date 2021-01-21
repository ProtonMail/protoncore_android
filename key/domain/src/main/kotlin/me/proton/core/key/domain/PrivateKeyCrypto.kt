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
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.crypto.common.pgp.unlockOrNull
import me.proton.core.crypto.common.simple.decrypt
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey

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
    context.pgpCrypto.getPublicKey(key).let { PublicKey(it, isPrimary) }

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
    requireNotNull(passphrase).decrypt(context.simpleCrypto).use { decrypted ->
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
    passphrase?.decrypt(context.simpleCrypto)?.use { decrypted ->
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
