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

import java.io.File

/**
 * @return [Armored] locked key, or `null` if [unlockedKey] cannot be locked using [passphrase].
 *
 * @see [PGPCrypto.lock]
 */
fun PGPCrypto.lockOrNull(
    unlockedKey: Unarmored,
    passphrase: ByteArray
): Armored? = runCatching { lock(unlockedKey, passphrase) }.getOrNull()

/**
 * @return [UnlockedKey], or `null` if [privateKey] cannot be unlocked using [passphrase].
 *
 * @see [PGPCrypto.unlock]
 */
fun PGPCrypto.unlockOrNull(
    privateKey: Armored,
    passphrase: ByteArray
): UnlockedKey? = runCatching { unlock(privateKey, passphrase) }.getOrNull()

/**
 * @return [String], or `null` if [message] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptText]
 */
fun PGPCrypto.decryptTextOrNull(
    message: EncryptedMessage,
    unlockedKey: Unarmored
): String? = runCatching { decryptText(message, unlockedKey) }.getOrNull()

/**
 * @return [ByteArray], or `null` if [message] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptData]
 */
fun PGPCrypto.decryptDataOrNull(
    message: EncryptedMessage,
    unlockedKey: Unarmored
): ByteArray? = runCatching { decryptData(message, unlockedKey) }.getOrNull()

/**
 * @return [ByteArray], or `null` if [data] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptData]
 */
fun PGPCrypto.decryptDataOrNull(
    data: DataPacket,
    sessionKey: SessionKey,
): ByteArray? = runCatching { decryptData(data, sessionKey) }.getOrNull()

/**
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptFile]
 */
fun PGPCrypto.decryptFileOrNull(
    source: EncryptedFile,
    destination: File,
    sessionKey: SessionKey
): DecryptedFile? = runCatching { decryptFile(source, destination, sessionKey) }.getOrNull()

/**
 * @return [SessionKey], or `null` if [keyPacket] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptData]
 */
fun PGPCrypto.decryptSessionKeyOrNull(
    keyPacket: KeyPacket,
    unlockedKey: Unarmored
): SessionKey? = runCatching { decryptSessionKey(keyPacket, unlockedKey) }.getOrNull()

/**
 * @return [Signature], or `null` if [plainText] cannot be signed.
 *
 * @see [PGPCrypto.signText]
 */
fun PGPCrypto.signTextOrNull(
    plainText: String,
    unlockedKey: Unarmored
): Signature? = runCatching { signText(plainText, unlockedKey) }.getOrNull()

/**
 * @return [Signature], or `null` if [data] cannot be signed.
 *
 * @see [PGPCrypto.signData]
 */
fun PGPCrypto.signDataOrNull(
    data: ByteArray,
    unlockedKey: Unarmored
): Signature? = runCatching { signData(data, unlockedKey) }.getOrNull()

/**
 * @return [Signature], or `null` if [file] cannot be signed.
 *
 * @see [PGPCrypto.signFile]
 */
fun PGPCrypto.signFileOrNull(
    file: File,
    unlockedKey: Unarmored
): Signature? = runCatching { signFile(file, unlockedKey) }.getOrNull()

/**
 * @return [EncryptedMessage], or `null` if [plainText] cannot be encrypted.
 *
 * @see [PGPCrypto.encryptText]
 */
fun PGPCrypto.encryptTextOrNull(
    plainText: String,
    publicKey: Armored
): EncryptedMessage? = runCatching { encryptText(plainText, publicKey) }.getOrNull()

/**
 * @return [EncryptedMessage], or `null` if [data] cannot be encrypted.
 *
 * @see [PGPCrypto.encryptData]
 */
fun PGPCrypto.encryptDataOrNull(
    data: ByteArray,
    publicKey: Armored
): EncryptedMessage? = runCatching { encryptData(data, publicKey) }.getOrNull()

/**
 * @return [EncryptedMessage], or `null` if [source] cannot be encrypted.
 *
 * @see [PGPCrypto.encryptData]
 */
fun PGPCrypto.encryptFileOrNull(
    source: File,
    destination: File,
    sessionKey: SessionKey
): EncryptedFile? = runCatching { encryptFile(source, destination, sessionKey) }.getOrNull()

/**
 * @return [EncryptedMessage], or `null` if [plainText] cannot be encrypted and signed.
 *
 * @see [PGPCrypto.encryptAndSignText]
 */
fun PGPCrypto.encryptAndSignTextOrNull(
    plainText: String,
    publicKey: Armored,
    unlockedKey: Unarmored
): EncryptedMessage? = runCatching { encryptAndSignText(plainText, publicKey, unlockedKey) }.getOrNull()

/**
 * @return [EncryptedMessage], or `null` if [data] cannot be encrypted and signed.
 *
 * @see [PGPCrypto.encryptAndSignData]
 */
fun PGPCrypto.encryptAndSignDataOrNull(
    data: ByteArray,
    publicKey: Armored,
    unlockedKey: Unarmored
): EncryptedMessage? = runCatching { encryptAndSignData(data, publicKey, unlockedKey) }.getOrNull()

/**
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedText], or `null` if [message] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptAndVerifyText]
 */
fun PGPCrypto.decryptAndVerifyTextOrNull(
    message: EncryptedMessage,
    publicKeys: List<Armored>,
    unlockedKeys: List<Unarmored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedText? = runCatching { decryptAndVerifyText(message, publicKeys, unlockedKeys, time) }.getOrNull()

/**
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedData], or `null` if [message] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptAndVerifyData]
 */
fun PGPCrypto.decryptAndVerifyDataOrNull(
    message: EncryptedMessage,
    publicKeys: List<Armored>,
    unlockedKeys: List<Unarmored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedData? = runCatching { decryptAndVerifyData(message, publicKeys, unlockedKeys, time) }.getOrNull()

/**
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [PGPCrypto.decryptAndVerifyFile]
 */
fun PGPCrypto.decryptAndVerifyFileOrNull(
    source: EncryptedFile,
    destination: File,
    sessionKey: SessionKey,
    publicKeys: List<Armored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile? =
    runCatching { decryptAndVerifyFile(source, destination, sessionKey, publicKeys, time) }.getOrNull()

/**
 * @return [Armored] public key, or `null` if public key cannot be extracted from [privateKey].
 *
 * @see [PGPCrypto.getPublicKey]
 */
fun PGPCrypto.getPublicKeyOrNull(
    privateKey: Armored
): Armored? = runCatching { getPublicKey(privateKey) }.getOrNull()

/**
 * @return fingerprint, or `null` if fingerprint cannot be extracted from [key].
 *
 * @see [PGPCrypto.getFingerprint]
 */
fun PGPCrypto.getFingerprintOrNull(
    key: Armored
): String? = runCatching { getFingerprint(key) }.getOrNull()

/**
 * @return updated [Armored] private key, or `null` if key cannot be updated.
 */
fun PGPCrypto.updatePrivateKeyPassphraseOrNull(
    privateKey: Armored,
    passphrase: ByteArray,
    newPassphrase: ByteArray
): Armored? = runCatching { updatePrivateKeyPassphrase(privateKey, passphrase, newPassphrase) }.getOrNull()
