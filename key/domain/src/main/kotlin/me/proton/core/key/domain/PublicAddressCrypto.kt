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
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext

/**
 * Verify [signature] of [text] is correctly signed using this [PublicAddress.publicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signText]
 */
fun PublicAddress.verifyText(
    context: CryptoContext,
    text: String,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = publicKeyRing().verifyText(context, text, signature, validAtUtc)

/**
 * Verify [signature] of [data] is correctly signed using this [PublicAddress.publicKeyRing].
 *
 * @param validAtUtc UTC time for [signature] validation, or 0 to ignore time.
 *
 * @return true if at least one [PublicKey] verify [signature].
 *
 * @see [PrivateKeyRing.signData]
 */
fun PublicAddress.verifyData(
    context: CryptoContext,
    data: ByteArray,
    signature: Signature,
    validAtUtc: Long = 0
): Boolean = publicKeyRing().verifyData(context, data, signature, validAtUtc)

/**
 * Encrypt [text] using this [PublicAddress.primaryKey].
 *
 * @throws [CryptoException] if [text] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptText]
 */
fun PublicAddress.encryptText(context: CryptoContext, text: String): EncryptedMessage =
    primaryKey.publicKey.encryptText(context, text)

/**
 * Encrypt [data] using this [PublicAddress.primaryKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptText]
 */
fun PublicAddress.encryptData(context: CryptoContext, data: ByteArray): EncryptedMessage =
    primaryKey.publicKey.encryptData(context, data)

/**
 * Get [PublicKeyRing] from this [PublicAddress].
 *
 * @return [PublicKeyRing] without any compromised keys.
 */
fun PublicAddress.publicKeyRing(): PublicKeyRing =
    PublicKeyRing(keys.mapNotNull { it.takeIf { !it.isCompromised }?.publicKey })
