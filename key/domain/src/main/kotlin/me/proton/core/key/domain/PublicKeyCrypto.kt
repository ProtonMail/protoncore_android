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
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKey].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKey.verifyText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Boolean = isActive && canVerify && context.pgpCrypto.verifyText(text, signature, key, time)

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKey].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicKey.verifyData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Boolean = isActive && canVerify && context.pgpCrypto.verifyData(data, signature, key, time)

/**
 * Verify [signature] of [file] is correctly signed using this [PublicKey].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicKey.verifyFile(
    context: CryptoContext,
    file: DecryptedFile,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Boolean = isActive && canVerify && context.pgpCrypto.verifyFile(file, signature, key, time)

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKey], and
 * return the timestamp if it is, null otherwise.
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKey.getVerifiedTimestampOfText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Long? = if (isActive && canVerify) {
    context.pgpCrypto.getVerifiedTimestampOfText(text, signature, key, time)
} else {
    null
}

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKey], and
 * return the timestamp if it is, null otherwise.
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKey.getVerifiedTimestampOfData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now
): Long? = if (isActive && canVerify) {
    context.pgpCrypto.getVerifiedTimestampOfData(data, signature, key, time)
} else {
    null
}

/**
 * Encrypt [text] using this [PublicKey].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun PublicKey.encryptText(context: CryptoContext, text: String): EncryptedMessage {
    if (!isActive) throw CryptoException("Key cannot be used while inactive.")
    if (!canEncrypt) throw CryptoException("Key cannot be used to encrypt.")
    return context.pgpCrypto.encryptText(text, key)
}

/**
 * Encrypt [data] using this [PublicKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun PublicKey.encryptData(context: CryptoContext, data: ByteArray): EncryptedMessage {
    if (!isActive) throw CryptoException("Key cannot be used while inactive.")
    if (!canEncrypt) throw CryptoException("Key cannot be used to encrypt.")
    return context.pgpCrypto.encryptData(data, key)
}

/**
 * Encrypt [sessionKey] using this [PublicKey].
 *
 * @throws [CryptoException] if [sessionKey] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptSessionKey]
 */
fun PublicKey.encryptSessionKey(context: CryptoContext, sessionKey: SessionKey): KeyPacket {
    if (!isActive) throw CryptoException("Key cannot be used while inactive.")
    if (!canEncrypt) throw CryptoException("Key cannot be used to encrypt.")
    return context.pgpCrypto.encryptSessionKey(sessionKey, key)
}

/**
 * Get fingerprint from this [PublicKey].
 *
 * @throws [CryptoException] if fingerprint cannot be extracted.
 */
fun PublicKey.fingerprint(context: CryptoContext) =
    context.pgpCrypto.getFingerprint(key)
