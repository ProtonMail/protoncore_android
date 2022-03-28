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

import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.pgp.exception.CryptoException
import java.io.File

/**
 * PGP Cryptographic interface (e.g. [lock], [unlock], [encryptData], [decryptData], [signData], [verifyData], ...).
 */
@Suppress("TooManyFunctions", "ComplexInterface")
interface PGPCrypto {

    /**
     * Lock [unlockedKey] using [passphrase].
     *
     * @throws [CryptoException] if [unlockedKey] cannot be locked using [passphrase].
     *
     * @see [unlock]
     */
    fun lock(unlockedKey: Unarmored, passphrase: ByteArray): Armored

    /**
     * Unlock [privateKey] using [passphrase].
     *
     * @return [UnlockedKey] implementing Closeable to clear memory after usage.
     *
     * @throws [CryptoException] if [privateKey] cannot be unlocked using [passphrase].
     *
     * @see [lock]
     */
    fun unlock(privateKey: Armored, passphrase: ByteArray): UnlockedKey

    /**
     * Encrypt [plainText] using [publicKey].
     *
     * @throws [CryptoException] if [plainText] cannot be encrypted.
     *
     * @see [decryptText].
     */
    fun encryptText(plainText: String, publicKey: Armored): EncryptedMessage

    /**
     * Encrypt [data] using [publicKey].
     *
     * @throws [CryptoException] if [data] cannot be encrypted.
     *
     * @see [decryptData].
     */
    fun encryptData(data: ByteArray, publicKey: Armored): EncryptedMessage

    /**
     * Encrypt [data] using [sessionKey].
     *
     * @throws [CryptoException] if [data] cannot be encrypted.
     *
     * @see [decryptData].
     */
    fun encryptData(data: ByteArray, sessionKey: SessionKey): DataPacket

    /**
     * Encrypt [source] into [destination] using [sessionKey].
     *
     * @throws [CryptoException] if [source] cannot be encrypted.
     *
     * @see [decryptFile].
     */
    fun encryptFile(source: File, destination: File, sessionKey: SessionKey): EncryptedFile

    /**
     * Encrypt [plainText] using [publicKey] and sign using [unlockedKey].
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [plainText] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyText].
     */
    fun encryptAndSignText(plainText: String, publicKey: Armored, unlockedKey: Unarmored): EncryptedMessage

    /**
     * Compress and encrypt [plainText] using [publicKey] and sign using [unlockedKey].
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [plainText] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyText].
     */
    fun encryptAndSignTextWithCompression(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage

    /**
     * Encrypt [data] using [publicKey] and sign using [unlockedKey].
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignData(data: ByteArray, publicKey: Armored, unlockedKey: Unarmored): EncryptedMessage

    /**
     * Compress and encrypt [data] using [publicKey] and sign using [unlockedKey].
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignDataWithCompression(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage

    /**
     * Encrypt [data] using [sessionKey] and sign using [unlockedKey].
     *
     * @return [DataPacket] with embedded signature.
     *
     * @throws [CryptoException] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignData(data: ByteArray, sessionKey: SessionKey, unlockedKey: Unarmored): DataPacket

    /**
     * Encrypt [source] into [destination] using [sessionKey] and sign using [unlockedKey].
     *
     * @throws [CryptoException] if [source] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyFile].
     */
    fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): EncryptedFile

    /**
     * Encrypt [sessionKey] using [publicKey].
     *
     * @throws [CryptoException] if [sessionKey] cannot be encrypted.
     *
     * @see [decryptSessionKey].
     */
    fun encryptSessionKey(sessionKey: SessionKey, publicKey: Armored): KeyPacket

    /**
     * Encrypt [sessionKey] using [password].
     *
     * @throws [CryptoException] if [sessionKey] cannot be encrypted.
     *
     * @see [decryptSessionKeyWithPassword].
     */
    fun encryptSessionKeyWithPassword(sessionKey: SessionKey, password: ByteArray): KeyPacket

    /**
     * Decrypt [message] as [String] using [unlockedKey].
     *
     * Note: String canonicalization/standardization is applied.
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [encryptText]
     */
    fun decryptText(message: EncryptedMessage, unlockedKey: Unarmored): String

    /**
     * Decrypt [message] as [ByteArray] using [unlockedKey].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [encryptData]
     */
    fun decryptData(message: EncryptedMessage, unlockedKey: Unarmored): ByteArray

    /**
     * Decrypt [data] as [ByteArray] using [sessionKey].
     *
     * @throws [CryptoException] if [data] cannot be decrypted.
     *
     * @see [encryptData]
     */
    fun decryptData(data: DataPacket, sessionKey: SessionKey): ByteArray

    /**
     * Decrypt [source] into [destination] using [sessionKey].
     *
     * @throws [CryptoException] if [source] cannot be decrypted.
     *
     * @see [encryptFile]
     */
    fun decryptFile(source: EncryptedFile, destination: File, sessionKey: SessionKey): DecryptedFile

    /**
     * Decrypt [message] as [String] using [unlockedKeys] and verify using [publicKeys].
     *
     * Note: String canonicalization/standardization is applied.
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [DecryptedText]
     * @see [VerificationStatus]
     * @see [encryptAndSignText]
     */
    fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime = VerificationTime.Now
    ): DecryptedText

    /**
     * Decrypt [message] as [ByteArray] using [unlockedKeys] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [DecryptedData]
     * @see [VerificationStatus]
     * @see [encryptAndSignData]
     */
    fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime = VerificationTime.Now
    ): DecryptedData

    /**
     * Decrypt [data] as [ByteArray] using [sessionKey] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @throws [CryptoException] if [data] cannot be decrypted.
     *
     * @see [DecryptedData]
     * @see [VerificationStatus]
     * @see [encryptAndSignData]
     */
    fun decryptAndVerifyData(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now
    ): DecryptedData

    /**
     * Decrypt [source] into [destination] using [sessionKey] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @throws [CryptoException] if [source] cannot be decrypted.
     *
     * @see [DecryptedFile]
     * @see [VerificationStatus]
     * @see [encryptAndSignFile]
     */
    fun decryptAndVerifyFile(
        source: EncryptedFile,
        destination: File,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now
    ): DecryptedFile

    /**
     * Decrypt [keyPacket] as [SessionKey] using [unlockedKey].
     *
     * @throws [CryptoException] if [keyPacket] cannot be decrypted.
     *
     * @see [encryptSessionKey]
     */
    fun decryptSessionKey(keyPacket: KeyPacket, unlockedKey: Unarmored): SessionKey

    /**
     * Decrypt [keyPacket] as [SessionKey] using [password].
     *
     * @throws [CryptoException] if [keyPacket] cannot be decrypted.
     *
     * @see [encryptSessionKey]
     */
    fun decryptSessionKeyWithPassword(keyPacket: KeyPacket, password: ByteArray): SessionKey

    /**
     * Sign [plainText] using [unlockedKey].
     *
     * @throws [CryptoException] if [plainText] cannot be signed.
     *
     * @see [verifyText]
     */
    fun signText(plainText: String, unlockedKey: Unarmored): Signature

    /**
     * Sign [data] using [unlockedKey].
     *
     * @throws [CryptoException] if [data] cannot be signed.
     *
     * @see [verifyData]
     */
    fun signData(data: ByteArray, unlockedKey: Unarmored): Signature

    /**
     * Sign [file] using [unlockedKey].
     *
     * @throws [CryptoException] if [file] cannot be signed.
     *
     * @see [verifyFile]
     */
    fun signFile(file: File, unlockedKey: Unarmored): Signature

    /**
     * Sign [plainText] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @throws [CryptoException] if [plainText] cannot be signed.
     *
     * @see [verifyTextEncrypted]
     */
    fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>
    ): EncryptedSignature

    /**
     * Sign [data] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @throws [CryptoException] if [data] cannot be signed.
     *
     * @see [verifyDataEncrypted]
     */
    fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>
    ): EncryptedSignature

    /**
     * Sign [file] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @throws [CryptoException] if [file] cannot be signed.
     *
     * @see [verifyFileEncrypted]
     */
    fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>
    ): EncryptedSignature

    /**
     * Verify [signature] of [plainText] is correctly signed using [publicKey].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @see [signText]
     */
    fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Verify [signature] of [data] is correctly signed using [publicKey].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @see [signData]
     */
    fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Verify [signature] of [file] is correctly signed using [publicKey].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @see [signFile]
     */
    fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Verify [signature] of [plainText] is correctly signed using [publicKey].
     * Returns the timestamp of the signature, or null if the signature is invalid
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @see [signText]
     */
    fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now
    ): Long?

    /**
     * Verify [signature] of [data] is correctly signed using [publicKey].
     * Returns the timestamp of the signature, or null if the signature is invalid
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @see [signData]
     */
    fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now
    ): Long?

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [plainText] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     *
     * @see [signTextEncrypted]
     */
    fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [data] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     *
     * @see [signDataEncrypted]
     */
    fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [file] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     *
     * @see [signFileEncrypted]
     */
    fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now
    ): Boolean

    /**
     * Get [Armored] from [data].
     *
     * @throws [CryptoException] if armored cannot be extracted from [data].
     */
    fun getArmored(data: Unarmored): Armored

    /**
     * Get [Unarmored] from [data].
     *
     * @throws [CryptoException] if unarmored cannot be extracted from [data].
     */
    fun getUnarmored(data: Armored): Unarmored

    /**
     * Get list of [EncryptedPacket] from [message].
     *
     * @throws [CryptoException] if key and data packets cannot be extracted from [message].
     */
    fun getEncryptedPackets(message: EncryptedMessage): List<EncryptedPacket>

    /**
     * Get [Armored] public key from [privateKey].
     *
     * @throws [CryptoException] if public key cannot be extracted from [privateKey].
     */
    fun getPublicKey(privateKey: Armored): Armored

    /**
     * Get fingerprint from [key].
     *
     * @throws [CryptoException] if fingerprint cannot be extracted from [key].
     */
    fun getFingerprint(key: Armored): String

    /**
     * Get JSON SHA256 fingerprints from [key] and corresponding sub-keys.
     *
     * @throws [CryptoException] if fingerprints cannot be extracted from [key].
     */
    fun getJsonSHA256Fingerprints(key: Armored): String

    /**
     * Get Base64 encoded string from [array].
     *
     * @see getBase64Decoded
     */
    fun getBase64Encoded(array: ByteArray): String

    /**
     * Get Base64 decoded array from [string].
     *
     * @see getBase64Encoded
     */
    fun getBase64Decoded(string: String): ByteArray

    /**
     * Get passphrase from [password] using [encodedSalt].
     *
     * Note: Consider using [use] on returned [ByteArray], to clear memory after usage.
     */
    fun getPassphrase(password: ByteArray, encodedSalt: String): ByteArray

    /**
     * Generate new SessionKey.
     *
     * Note: Consider using [use] on returned [SessionKey], to clear memory after usage.
     */
    fun generateNewSessionKey(): SessionKey

    /**
     * Generate new HashKey.
     *
     * Note: Consider using [use] on returned [HashKey], to clear memory after usage.
     */
    fun generateNewHashKey(): HashKey

    /**
     * Generate new random salt.
     *
     * @return 16-byte, base64-ed random salt as String, without newline character.
     */
    fun generateNewKeySalt(): String

    /**
     * Generate new random Token.
     *
     * Default token size is 32 bytes.
     */
    fun generateNewToken(size: Long = 32): ByteArray

    /**
     * Generate new private key of type [KeyType.X25519].
     *
     * @param username for which the key is generated.
     * @param domain for which the key is generated.
     * @param passphrase passphrase to lock the key.
     *
     * @throws [CryptoException] if key cannot be generated.
     */
    fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray
    ): Armored

    /**
     * Update private key passphrase.
     *
     * @param privateKey [Armored] private key to change.
     * @param passphrase  current passphrase to unlock the key.
     * @param newPassphrase new passphrase to set.
     *
     * @throws [CryptoException] if key cannot be updated.
     */
    fun updatePrivateKeyPassphrase(
        privateKey: Armored,
        passphrase: ByteArray,
        newPassphrase: ByteArray
    ): Armored

    /**
     * Update the current time used for crypto function (e.g. signing).
     *
     * @param epochSeconds Number of seconds from Epoch (1970-01-01T00:00:00Z).
     */
    fun updateTime(epochSeconds: Long)

    enum class KeyType(private val value: String) {
        X25519("x25519");

        override fun toString(): String = value
    }
}
