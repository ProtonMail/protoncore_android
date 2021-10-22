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
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import me.proton.core.crypto.common.pgp.decryptFileOrNull
import me.proton.core.crypto.common.pgp.exception.CryptoException
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
 * Decrypt [data] using this [SessionKey].
 *
 * @throws [CryptoException] if [data] cannot be encrypted.
 *
 * @see [SessionKey.encryptData]
 */
fun SessionKey.decryptData(
    context: CryptoContext,
    data: DataPacket,
): ByteArray = context.pgpCrypto.decryptData(data, this)

/**
 * Decrypt [data] using this [SessionKey].
 *
 * @return [ByteArray], or `null` if [data] cannot be decrypted.
 *
 * @see [SessionKey.decryptData]
 */
fun SessionKey.decryptDataOrNull(
    context: CryptoContext,
    data: DataPacket,
): ByteArray? = context.pgpCrypto.decryptDataOrNull(data, this)

/**
 * Decrypt [source] into [destination] using this [SessionKey].
 *
 * @throws [CryptoException] if [source] cannot be encrypted.
 *
 * @see [SessionKey.encryptFile]
 */
fun SessionKey.decryptFile(
    context: CryptoContext,
    source: File,
    destination: File
): DecryptedFile = context.pgpCrypto.decryptFile(source, destination, this)

/**
 * Decrypt [source] into [destination] using this [SessionKey].
 *
 * @return [DecryptedFile], or `null` if [source] cannot be decrypted.
 *
 * @see [SessionKey.encryptFile]
 */
fun SessionKey.decryptFileOrNull(
    context: CryptoContext,
    source: EncryptedFile,
    destination: File
): DecryptedFile? = context.pgpCrypto.decryptFileOrNull(source, destination, this)
