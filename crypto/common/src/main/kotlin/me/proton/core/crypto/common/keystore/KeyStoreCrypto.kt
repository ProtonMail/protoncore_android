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

package me.proton.core.crypto.common.keystore

import me.proton.core.util.kotlin.CoreLogger

/**
 * KeyStore Cryptographic interface providing [encrypt] function on [String] and [PlainByteArray],
 * and a [decrypt] function on [EncryptedString] and [EncryptedByteArray].
 */
interface KeyStoreCrypto {
    /**
     * Returns whether System Keystore is being used providing secure key, or false otherwise.
     */
    fun isUsingKeyStore(): Boolean

    /**
     * Encrypt a [String] [value] and return an [EncryptedString].
     */
    fun encrypt(value: String): EncryptedString

    /**
     * Decrypt an [EncryptedString] [value] and return a [String].
     */
    fun decrypt(value: EncryptedString): String

    /**
     * Encrypt a [PlainByteArray] [value] and return an [EncryptedByteArray].
     */
    fun encrypt(value: PlainByteArray): EncryptedByteArray

    /**
     * Decrypt an [EncryptedByteArray] [value] and return a [PlainByteArray].
     */
    fun decrypt(value: EncryptedByteArray): PlainByteArray
}

/**
 * Returns encrypted value, or the result of [onFailure] function on encryption failure.
 *
 * @see [KeyStoreCrypto.encrypt]
 */
fun KeyStoreCrypto.encryptOrElse(
    value: String,
    onFailure: (Throwable) -> EncryptedString?
): EncryptedString? = runCatching {
    encrypt(value)
}.getOrElse {
    CoreLogger.e(LogTag.KEYSTORE_ENCRYPT, it)
    onFailure(it)
}

/**
 * Returns encrypted value, or the result of [onFailure] function on encryption failure.
 *
 * @see [KeyStoreCrypto.encrypt]
 */
fun KeyStoreCrypto.encryptOrElse(
    value: PlainByteArray,
    onFailure: (Throwable) -> EncryptedByteArray?
): EncryptedByteArray? = runCatching {
    encrypt(value)
}.getOrElse {
    CoreLogger.e(LogTag.KEYSTORE_ENCRYPT, it)
    onFailure(it)
}

/**
 * Returns decrypted value, or the result of [onFailure] function on decryption failure.
 *
 * @see [KeyStoreCrypto.decrypt]
 */
fun KeyStoreCrypto.decryptOrElse(
    value: EncryptedString,
    onFailure: (Throwable) -> String?
): String? = runCatching {
    decrypt(value)
}.getOrElse {
    CoreLogger.e(LogTag.KEYSTORE_DECRYPT, it)
    onFailure(it)
}

/**
 * Returns decrypted value, or the result of [onFailure] function on decryption failure.
 *
 * @see [KeyStoreCrypto.decrypt]
 */
fun KeyStoreCrypto.decryptOrElse(
    value: EncryptedByteArray,
    onFailure: (Throwable) -> PlainByteArray?
): PlainByteArray? = runCatching {
    decrypt(value)
}.getOrElse {
    CoreLogger.e(LogTag.KEYSTORE_DECRYPT, it)
    onFailure(it)
}

object LogTag {
    /** Tag for KeyStore initialization check failure. */
    const val KEYSTORE_INIT = "core.crypto.common.keystore.init"

    /** Tag for KeyStore initialization check failure retry. */
    const val KEYSTORE_INIT_RETRY = "core.crypto.common.keystore.init.retry"

    /** Tag for KeyStore initialization add key. */
    const val KEYSTORE_INIT_ADD_KEY = "core.crypto.common.keystore.init.add.key"

    /** Tag for KeyStore initialization delete key. */
    const val KEYSTORE_INIT_DELETE_KEY = "core.crypto.common.keystore.init.delete.key"

    /** Tag for KeyStore encrypt failure. */
    const val KEYSTORE_ENCRYPT = "core.crypto.common.keystore.encrypt"

    /** Tag for KeyStore encrypt failure retry. */
    const val KEYSTORE_ENCRYPT_RETRY = "core.crypto.common.keystore.encrypt.retry"

    /** Tag for KeyStore decrypt failure. */
    const val KEYSTORE_DECRYPT = "core.crypto.common.keystore.decrypt"

    /** Tag for KeyStore decrypt failure retry. */
    const val KEYSTORE_DECRYPT_RETRY = "core.crypto.common.keystore.decrypt.retry"
}
