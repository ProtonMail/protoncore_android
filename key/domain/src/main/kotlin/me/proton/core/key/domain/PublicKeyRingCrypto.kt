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
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing

/**
 * Encrypt [text] using this [PublicKeyRing.primaryKey].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [PrivateKeyRing.decryptText]
 */
fun PublicKeyRing.encryptText(context: CryptoContext, text: String): EncryptedMessage =
    primaryKey.encryptText(context, text)

/**
 * Encrypt [data] using this [PublicKeyRing.primaryKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [PrivateKeyRing.decryptText]
 */
fun PublicKeyRing.encryptData(context: CryptoContext, data: ByteArray): EncryptedMessage =
    primaryKey.encryptData(context, data)

/**
 * Encrypt [sessionKey] using this [PublicKeyRing.primaryKey].
 *
 * @throws [CryptoException] if [sessionKey] cannot be encrypted.
 *
 * @see [PrivateKeyRing.decryptSessionKey]
 */
fun PublicKeyRing.encryptSessionKey(context: CryptoContext, sessionKey: SessionKey): KeyPacket =
    primaryKey.encryptSessionKey(context, sessionKey)

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 * @param verificationContext: If set, the context is used to verify the signature was made in the right context.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKeyRing.verifyText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    trimTrailingSpaces: Boolean = true,
    verificationContext: VerificationContext? = null
): Boolean = keys.any { it.verifyText(context, text, signature, time, trimTrailingSpaces, verificationContext) }

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param verificationContext: If set, the context is used to verify the signature was made in the right context.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKeyRing.verifyData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    verificationContext: VerificationContext? = null
): Boolean = keys.any { it.verifyData(context, data, signature, time, verificationContext) }

/**
 * Verify [signature] of [file] is correctly signed using this [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param verificationContext: If set, the context is used to verify the signature was made in the right context.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signFile]
 */
fun PublicKeyRing.verifyFile(
    context: CryptoContext,
    file: DecryptedFile,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    verificationContext: VerificationContext? = null
): Boolean = keys.any { it.verifyFile(context, file, signature, time, verificationContext) }

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param trimTrailingSpaces: If set to true, each line end will be trimmed of all trailing spaces and tabs,
 * before signing the message.
 * Trimming trailing spaces used to be the default behavior of the library.
 * This might be needed in some cases to respect a standard, or to maintain compatibility with old signatures.
 * @param verificationContext: If set, the context is used to verify the signature was made in the right context.
 *
 * @return the timestamp of the signature if at least one [PublicKey] verify [signature]. null otherwise
 *
 * @see [PrivateKeyRing.signText]
 */

fun PublicKeyRing.getVerifiedTimestampOfText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    trimTrailingSpaces: Boolean = true,
    verificationContext: VerificationContext? = null
): Long? = keys
    .asSequence()
    .mapNotNull { key ->
        key.getVerifiedTimestampOfText(
            context,
            text,
            signature,
            time,
            trimTrailingSpaces,
            verificationContext
        )
    }
    .firstOrNull()

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKeyRing].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 * @param verificationContext: If set, the context is used to verify the signature was made in the right context.
 *
 * @return the timestamp of the signature if at least one [PublicKey] verify [signature]. null otherwise
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicKeyRing.getVerifiedTimestampOfData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    time: VerificationTime = VerificationTime.Now,
    verificationContext: VerificationContext? = null
): Long? = keys
    .asSequence()
    .mapNotNull { key -> key.getVerifiedTimestampOfData(context, data, signature, time, verificationContext) }
    .firstOrNull()
