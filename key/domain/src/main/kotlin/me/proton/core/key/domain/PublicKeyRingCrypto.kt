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
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PlainFile
import me.proton.core.crypto.common.pgp.Signature
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
 * Encrypt [file] using this [PublicKeyRing.primaryKey].
 *
 * @throws [CryptoException] if [file] cannot be encrypted.
 *
 * @see [PrivateKeyRing.decryptFile]
 */
fun PublicKeyRing.encryptFile(context: CryptoContext, file: PlainFile): EncryptedFile =
    primaryKey.encryptFile(context, file)

/**
 * Encrypt [keyPacket] using this [PublicKeyRing.primaryKey].
 *
 * @throws [CryptoException] if [keyPacket] cannot be encrypted.
 *
 * @see [PrivateKeyRing.decryptSessionKey]
 */
fun PublicKeyRing.encryptSessionKey(context: CryptoContext, keyPacket: KeyPacket): ByteArray =
    primaryKey.encryptSessionKey(context, keyPacket)

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKeyRing.verifyText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = keys.any { it.verifyText(context, text, signature, validAtUtc) }

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKeyRing.verifyData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = keys.any { it.verifyData(context, data, signature, validAtUtc) }

/**
 * Verify [signature] of [file] is correctly signed using this [PublicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signFile]
 */
fun PublicKeyRing.verifyFile(
    context: CryptoContext,
    file: DecryptedFile,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = keys.any { it.verifyFile(context, file, signature, validAtUtc) }
