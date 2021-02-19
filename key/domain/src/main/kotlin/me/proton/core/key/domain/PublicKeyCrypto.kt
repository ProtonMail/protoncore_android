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
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey

/**
 * Verify [signature] of [text] is correctly signed using this [PublicKey].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicKey.verifyText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = context.pgpCrypto.verifyText(text, signature, key, validAtUtc)

/**
 * Verify [signature] of [data] is correctly signed using this [PublicKey].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicKey.verifyData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = context.pgpCrypto.verifyData(data, signature, key, validAtUtc)

/**
 * Verify [signature] of [file] is correctly signed using this [PublicKey].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicKey.verifyFile(
    context: CryptoContext,
    file: DecryptedFile,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = context.pgpCrypto.verifyFile(file, signature, key, validAtUtc)

/**
 * Encrypt [text] using this [PublicKey].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun PublicKey.encryptText(context: CryptoContext, text: String): EncryptedMessage =
    context.pgpCrypto.encryptText(text, key)

/**
 * Encrypt [data] using this [PublicKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun PublicKey.encryptData(context: CryptoContext, data: ByteArray): EncryptedMessage =
    context.pgpCrypto.encryptData(data, key)

/**
 * Encrypt [file] using this [PublicKey].
 *
 * @throws [CryptoException] if [file] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptText]
 */
fun PublicKey.encryptFile(context: CryptoContext, file: PlainFile): EncryptedFile =
    context.pgpCrypto.encryptFile(file, key)

/**
 * Encrypt [keyPacket] using this [PublicKey].
 *
 * @throws [CryptoException] if [keyPacket] cannot be encrypted.
 *
 * @see [UnlockedPrivateKey.decryptSessionKey]
 */
fun PublicKey.encryptSessionKey(context: CryptoContext, keyPacket: KeyPacket): ByteArray =
    context.pgpCrypto.encryptSessionKey(keyPacket, key)

/**
 * Get fingerprint from this [PublicKey].
 *
 * @throws [CryptoException] if fingerprint cannot be extracted.
 */
fun PublicKey.fingerprint(context: CryptoContext) =
    context.pgpCrypto.getFingerprint(key)
