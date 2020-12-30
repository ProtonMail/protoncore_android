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

package me.proton.core.crypto.common.pgp

import me.proton.core.crypto.common.simple.use

/**
 * PGP Cryptographic interface (e.g. [lock], [unlock], [encryptData], [decryptData], [signData], [verifyData], ...).
 */
interface PGPCrypto {

    /**
     * Lock [unlockedKey] using [passphrase].
     *
     * @throws [Throwable] if [unlockedKey] cannot be locked using [passphrase].
     *
     * @see [unlock]
     */
    fun lock(unlockedKey: Unarmored, passphrase: ByteArray): Armored

    /**
     * Unlock [privateKey] using [passphrase].
     *
     * @return [UnlockedKey] implementing Closeable to clear memory after usage.
     *
     * @throws [Throwable] if [privateKey] cannot be unlocked using [passphrase].
     *
     * @see [lock]
     */
    fun unlock(privateKey: Armored, passphrase: ByteArray): UnlockedKey

    /**
     * Sign [plainText] using [unlockedKey].
     *
     * @throws [Throwable] if [plainText] cannot be signed.
     *
     * @see [verifyText]
     */
    fun signText(plainText: String, unlockedKey: Unarmored): Signature

    /**
     * Sign [data] using [unlockedKey].
     *
     * @throws [Throwable] if [data] cannot be signed.
     *
     * @see [verifyData]
     */
    fun signData(data: ByteArray, unlockedKey: Unarmored): Signature

    /**
     * Verify [signature] of [plainText] is correctly signed using [publicKey].
     *
     * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
     *
     * @see [signText]
     */
    fun verifyText(plainText: String, signature: Armored, publicKey: Armored, validAtUtc: Long): Boolean

    /**
     * Verify [signature] of [data] is correctly signed using [publicKey].
     *
     * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
     *
     * @see [signData]
     */
    fun verifyData(data: ByteArray, signature: Armored, publicKey: Armored, validAtUtc: Long): Boolean

    /**
     * Decrypt [message] as [String] using [unlockedKey].
     *
     * Note: String canonicalization/standardization is applied.
     *
     * @throws [Throwable] if [message] cannot be decrypted.
     *
     * @see [encryptText]
     */
    fun decryptText(message: EncryptedMessage, unlockedKey: Unarmored): String

    /**
     * Decrypt [message] as [ByteArray] using [unlockedKey].
     *
     * @throws [Throwable] if [message] cannot be decrypted.
     *
     * @see [encryptData]
     */
    fun decryptData(message: EncryptedMessage, unlockedKey: Unarmored): ByteArray

    /**
     * Encrypt [plainText] using [publicKey].
     *
     * @throws [Throwable] if [plainText] cannot be encrypted.
     *
     * @see [decryptText].
     */
    fun encryptText(plainText: String, publicKey: Armored): EncryptedMessage

    /**
     * Encrypt [data] using [publicKey].
     *
     * @throws [Throwable] if [data] cannot be encrypted.
     *
     * @see [decryptData].
     */
    fun encryptData(data: ByteArray, publicKey: Armored): EncryptedMessage

    /**
     * Encrypt [plainText] using [publicKey] and sign using [unlockedKey] in an embedded [EncryptedMessage].
     *
     * @throws [Throwable] if [plainText] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyText].
     */
    fun encryptAndSignText(plainText: String, publicKey: Armored, unlockedKey: Unarmored): EncryptedMessage

    /**
     * Encrypt [data] using [publicKey] and sign using [unlockedKey] in an embedded [EncryptedMessage].
     *
     * @throws [Throwable] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignData(data: ByteArray, publicKey: Armored, unlockedKey: Unarmored): EncryptedMessage

    /**
     * Decrypt [message] as [String] using [unlockedKeys] and verify using [publicKeys].
     *
     * Note: String canonicalization/standardization is applied.
     *
     * @throws [Throwable] if [message] cannot be decrypted or verified.
     *
     * @see [encryptAndSignText]
     */
    fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>
    ): String

    /**
     * Decrypt [message] as [ByteArray] using [unlockedKeys] and verify using [publicKeys].
     *
     * @throws [Throwable] if [message] cannot be decrypted or verified.
     *
     * @see [encryptAndSignData]
     */
    fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>
    ): ByteArray

    /**
     * Get [Armored] public key from [privateKey].
     *
     * @throws [Throwable] if public key cannot be extracted from [privateKey].
     */
    fun getPublicKey(privateKey: Armored): Armored

    /**
     * Get fingerprint from [key].
     *
     * @throws [Throwable] if fingerprint cannot be extracted from [key].
     */
    fun getFingerprint(key: Armored): String

    /**
     * Get passphrase from [password] using [encodedSalt].
     *
     * Note: Consider using [use] on returned [ByteArray], to clear memory after usage.
     */
    fun getPassphrase(password: ByteArray, encodedSalt: String): ByteArray
}
