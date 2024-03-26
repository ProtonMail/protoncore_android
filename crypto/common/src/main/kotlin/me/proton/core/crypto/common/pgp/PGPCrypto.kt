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
     * Check if an armored representation of [key] belongs to a public key.
     */
    fun isPublicKey(key: Armored): Boolean

    /**
     * Check if an armored representation of [key] belongs to a private key.
     */
    fun isPrivateKey(key: Armored): Boolean

    /**
     * Check if an armored representation of [key] is valid.
     */
    fun isValidKey(key: Armored): Boolean

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
     * Encrypt [text] using [password].
     *
     * @throws [CryptoException] if [text] cannot be encrypted.
     *
     * @see [decryptTextWithPassword]
     */
    fun encryptTextWithPassword(text: String, password: ByteArray): EncryptedMessage

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
     * Encrypt [data] using [password].
     *
     * @throws [CryptoException] if [data] cannot be encrypted.
     *
     * @see [decryptDataWithPassword]
     */
    fun encryptDataWithPassword(data: ByteArray, password: ByteArray): EncryptedMessage

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
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [plainText] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyText].
     */
    fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
    ): EncryptedMessage

    /**
     * Compress and encrypt [plainText] using [publicKey] and sign using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
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
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
    ): EncryptedMessage

    /**
     * Encrypt [data] using [publicKey] and sign using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @return [EncryptedMessage] with embedded signature.
     *
     * @throws [CryptoException] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
    ): EncryptedMessage

    /**
     * Compress and encrypt [data] using [publicKey] and sign using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
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
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
    ): EncryptedMessage

    /**
     * Encrypt [data] using [sessionKey] and sign using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @return [DataPacket] with embedded signature.
     *
     * @throws [CryptoException] if [data] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyData].
     */
    fun encryptAndSignData(
        data: ByteArray,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
    ): DataPacket

    /**
     * Encrypt [source] into [destination] using [sessionKey] and sign using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [source] cannot be encrypted or signed.
     *
     * @see [decryptAndVerifyFile].
     */
    fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext? = null
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
     * Encrypt the encrypted [message] to an additional [publicKey] with the [unlockedKey].
     * The function internally decrypts the session key with [unlockedKey], encrypts the session key
     * with [publicKey], and adds the additional key packet to the message.
     *
     * @return [EncryptedMessage] that contains an additional key packet encrypted with [publicKey].
     *
     * @throws [CryptoException] if [message] cannot be encrypted to the additional key.
     */
    fun encryptMessageToAdditionalKey(
        message: EncryptedMessage,
        unlockedKey: Unarmored,
        publicKey: Armored,
    ): EncryptedMessage

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
     * Decrypt [message] as [String] using [password].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [encryptTextWithPassword]
     */
    fun decryptTextWithPassword(message: EncryptedMessage, password: ByteArray): String

    /**
     * Decrypt a PGP/MIME [message] using [unlockedKeys], and parse the decrypted content.
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @return a [DecryptedMimeMessage] with the parsed content.
     *
     */
    fun decryptMimeMessage(message: EncryptedMessage, unlockedKeys: List<Unarmored>): DecryptedMimeMessage

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
     * Decrypt [message] as [Byte] using [password].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @see [encryptDataWithPassword]
     */
    fun decryptDataWithPassword(message: EncryptedMessage, password: ByteArray): ByteArray

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
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
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
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): DecryptedText

    /**
     * Decrypt a PGP/MIME [message] using [unlockedKeys], verify using [publicKeys], and parse the decrypted content.
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @throws [CryptoException] if [message] cannot be decrypted.
     *
     * @return a [DecryptedMimeMessage] with the parsed content and [VerificationStatus]
     *
     * @see [DecryptedMimeMessage]
     * @see [VerificationStatus]
     */
    fun decryptAndVerifyMimeMessage(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime = VerificationTime.Now
    ): DecryptedMimeMessage

    /**
     * Decrypt [message] as [ByteArray] using [unlockedKeys] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
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
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): DecryptedData

    /**
     * Decrypt [data] as [ByteArray] using [sessionKey] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
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
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): DecryptedData

    /**
     * Decrypt [source] into [destination] using [sessionKey] and verify using [publicKeys].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
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
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
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
     * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
     * before signing the message.
     * Trimming trailing spaces used to be the default behavior of the library.
     * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [plainText] cannot be signed.
     *
     * @see [verifyText]
     */
    fun signText(
        plainText: String,
        unlockedKey: Unarmored,
        trimTrailingSpaces: Boolean = true,
        signatureContext: SignatureContext? = null
    ): Signature

    /**
     * Sign [data] using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [data] cannot be signed.
     *
     * @see [verifyData]
     */
    fun signData(data: ByteArray, unlockedKey: Unarmored, signatureContext: SignatureContext? = null): Signature

    /**
     * Sign [file] using [unlockedKey].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [file] cannot be signed.
     *
     * @see [verifyFile]
     */
    fun signFile(file: File, unlockedKey: Unarmored, signatureContext: SignatureContext? = null): Signature

    /**
     * Sign [plainText] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
     * before signing the message.
     * Trimming trailing spaces used to be the default behavior of the library.
     * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [plainText] cannot be signed.
     *
     * @see [verifyTextEncrypted]
     */
    fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        trimTrailingSpaces: Boolean = true,
        signatureContext: SignatureContext? = null
    ): EncryptedSignature

    /**
     * Sign [data] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [data] cannot be signed.
     *
     * @see [verifyDataEncrypted]
     */
    fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext? = null
    ): EncryptedSignature

    /**
     * Sign [file] using [unlockedKey] and encrypt the signature using [encryptionKeys].
     *
     * @param signatureContext: If a context is given, it is included in the signature as notation data.
     *
     * @throws [CryptoException] if [file] cannot be signed.
     *
     * @see [verifyFileEncrypted]
     */
    fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext? = null
    ): EncryptedSignature

    /**
     * Verify [signature] of [plainText] is correctly signed using [publicKey].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
     * before verifying the message.
     * Trimming trailing spaces used to be the default behavior of the library.
     * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     *
     * @see [signText]
     */
    fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now,
        trimTrailingSpaces: Boolean = true,
        verificationContext: VerificationContext? = null
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
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): Boolean

    /**
     * Verify [signature] of [file] is correctly signed using [publicKey].
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signFile]
     */
    fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): Boolean

    /**
     * Verify [signature] of [plainText] is correctly signed using [publicKey].
     * Returns the timestamp of the signature, or null if the signature is invalid
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
     * before verifying the message.
     * Trimming trailing spaces used to be the default behavior of the library.
     * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signText]
     */
    fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now,
        trimTrailingSpaces: Boolean = true,
        verificationContext: VerificationContext? = null
    ): Long?

    /**
     * Verify [signature] of [data] is correctly signed using [publicKey].
     * Returns the timestamp of the signature, or null if the signature is invalid
     *
     * @param time time for embedded signature validation, default to [VerificationTime.Now].
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signData]
     */
    fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): Long?

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [plainText] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
     * before verifying the message.
     * Trimming trailing spaces used to be the default behavior of the library.
     * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signTextEncrypted]
     */
    fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now,
        trimTrailingSpaces: Boolean = true,
        verificationContext: VerificationContext? = null
    ): Boolean

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [data] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signDataEncrypted]
     */
    fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): Boolean

    /**
     * Decrypt [encryptedSignature] with [privateKey]
     * and then verify it is a valid signature of [file] using [publicKeys].
     *
     * @param time time for encrypted signature validation, default to [VerificationTime.Now].
     *
     * @param verificationContext: If a context is given, the signature is verified using the context.
     * The context requirement condition is enforced by the verification.
     *
     * @see [signFileEncrypted]
     */
    fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime = VerificationTime.Now,
        verificationContext: VerificationContext? = null
    ): Boolean

    /**
     * Get [Armored] from [data].
     *
     * Note: By default, use [PGPHeader.Message].
     *
     * @throws [CryptoException] if armored cannot be extracted from [data].
     */
    fun getArmored(data: Unarmored, header: PGPHeader = PGPHeader.Message): Armored

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
     * Checks if armored Key is expired.
     */
    fun isKeyExpired(key: Armored): Boolean

    /**
     * Checks if armored Key is revoked.
     */
    fun isKeyRevoked(key: Armored): Boolean

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
     * Note: The tokens are encoded in hexadecimal
     * and then treated as bytes.
     */
    fun generateNewToken(size: Long = 32): ByteArray

    /**
     * Generate a new random byte array.
     *
     * Default token size is 32 bytes.
     * Note: contrary to [generateNewToken], this function
     * doesn't apply any kind of encoding to the bytes generated
     */
    fun generateRandomBytes(size: Long = 32): ByteArray

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

    /**
     * Get the current time (unix epoch in seconds) used for crypto functions (e.g. signing)
     */
    suspend fun getCurrentTime(): Long

    enum class KeyType(private val value: String) {
        X25519("x25519");

        override fun toString(): String = value
    }
}
