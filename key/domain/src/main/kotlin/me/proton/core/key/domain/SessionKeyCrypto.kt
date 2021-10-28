/*
 * Copyright (c) 2021 Proton Technologies AG
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
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.crypto.common.pgp.decryptAndVerifyDataOrNull
import me.proton.core.crypto.common.pgp.decryptAndVerifyFileOrNull
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import me.proton.core.crypto.common.pgp.decryptFileOrNull
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import java.io.File

/**
 * Encrypt [byteArray] using this [SessionKey].
 *
 * @throws [CryptoException] if [byteArray] cannot be encrypted.
 *
 * @see [SessionKey.decryptData]
 */
fun SessionKey.encryptData(
    context: CryptoContext,
    byteArray: ByteArray,
): DataPacket = context.pgpCrypto.encryptData(byteArray, this)

/**
 * Encrypt [byteArray] using this [SessionKey] and sign using [unlockedKey].
 *
 * @throws [CryptoException] if [byteArray] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptData]
 */
fun SessionKey.encryptAndSignData(
    context: CryptoContext,
    byteArray: ByteArray,
    unlockedKey: Unarmored,
): DataPacket = context.pgpCrypto.encryptAndSignData(byteArray, this, unlockedKey)

/**
 * Encrypt [source] into [destination] using this [SessionKey].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [SessionKey.decryptFile]
 */
fun SessionKey.encryptFile(
    context: CryptoContext,
    source: File,
    destination: File,
): EncryptedFile = context.pgpCrypto.encryptFile(source, destination, this)

/**
 * Encrypt [source] using this [SessionKey] and sign using [unlockedKey].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [KeyHolderContext.decryptAndVerifyFile]
 */
fun SessionKey.encryptAndSignFile(
    context: CryptoContext,
    source: File,
    destination: File,
    unlockedKey: Unarmored
): EncryptedFile = context.pgpCrypto.encryptAndSignFile(source, destination, this, unlockedKey)

/**
 * Decrypt [data] using this [SessionKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [SessionKey.encryptData]
 * @see [SessionKey.decryptDataOrNull]
 */
fun SessionKey.decryptData(
    context: CryptoContext,
    data: DataPacket,
): ByteArray = context.pgpCrypto.decryptData(data, this)

/**
 * Decrypt [data] using this [SessionKey] and verify using [publicKeys].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [SessionKey.encryptAndSignData]
 * @see [SessionKey.decryptAndVerifyDataOrNull]
 */
fun SessionKey.decryptAndVerifyData(
    context: CryptoContext,
    data: DataPacket,
    publicKeys: List<Armored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedData = context.pgpCrypto.decryptAndVerifyData(data, this, publicKeys, time)

/**
 * Decrypt [source] into [destination] using this [SessionKey].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [SessionKey.encryptFile]
 * @see [SessionKey.decryptFileOrNull]
 */
fun SessionKey.decryptFile(
    context: CryptoContext,
    source: File,
    destination: File
): DecryptedFile = context.pgpCrypto.decryptFile(source, destination, this)

/**
 * Decrypt [source] using this [SessionKey] and verify using [publicKeys].
 *
 * @param time time for embedded signature validation, default to [VerificationTime.Now].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [SessionKey.encryptAndSignFile]
 * @see [SessionKey.decryptAndVerifyFileOrNull]
 */
fun SessionKey.decryptAndVerifyFile(
    context: CryptoContext,
    source: EncryptedFile,
    destination: File,
    publicKeys: List<Armored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile = context.pgpCrypto.decryptAndVerifyFile(source, destination, this, publicKeys, time)

/**
 * Decrypt [data] using this [SessionKey].
 *
 * @return [ByteArray], or `null` if [data] cannot be decrypted.
 *
 * @see [SessionKey.encryptData]
 * @see [SessionKey.decryptData]
 */
fun SessionKey.decryptDataOrNull(
    context: CryptoContext,
    data: DataPacket,
): ByteArray? = context.pgpCrypto.decryptDataOrNull(data, this)

/**
 * Decrypt [data] using this [SessionKey] and verify using [publicKeys].
 *
 * @return [DecryptedFile], or `null` if [data] cannot be decrypted.
 *
 * @see [SessionKey.encryptAndSignData]
 * @see [SessionKey.decryptAndVerifyData]
 */
fun SessionKey.decryptAndVerifyDataOrNull(
    context: CryptoContext,
    data: DataPacket,
    publicKeys: List<Armored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedData? = context.pgpCrypto.decryptAndVerifyDataOrNull(data, this, publicKeys, time)

/**
 * Decrypt [source] into [destination] using this [SessionKey].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [SessionKey.encryptFile]
 * @see [SessionKey.decryptFile]
 */
fun SessionKey.decryptFileOrNull(
    context: CryptoContext,
    source: EncryptedFile,
    destination: File
): DecryptedFile? = context.pgpCrypto.decryptFileOrNull(source, destination, this)

/**
 * Decrypt [source] using this [SessionKey] and verify using [publicKeys].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [SessionKey.encryptAndSignFile]
 * @see [SessionKey.decryptAndVerifyFile]
 */
fun SessionKey.decryptAndVerifyFileOrNull(
    context: CryptoContext,
    source: EncryptedFile,
    destination: File,
    publicKeys: List<Armored>,
    time: VerificationTime = VerificationTime.Now
): DecryptedFile? = context.pgpCrypto.decryptAndVerifyFileOrNull(source, destination, this, publicKeys, time)
