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

package me.proton.core.crypto.android.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.LogTag
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.use
import me.proton.core.util.kotlin.CoreLogger
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * [KeyStoreCrypto] implementation based on Android KeyStore System.
 *
 * @see <a href="https://developer.android.com/training/articles/keystore">Android KeyStore System</a>
 * @see [KeyStore]
 * @see [KeyGenParameterSpec]
 */
class AndroidKeyStoreCrypto private constructor(
    masterKeyAlias: String = DEFAULT_MASTER_KEY_ALIAS
) : KeyStoreCrypto {

    private val androidKeyStore = "AndroidKeyStore"
    private val cipherTransformation = "AES/GCM/NoPadding"
    private val cipherIvBytes = 12
    private val cipherGCMTagBits = 128
    private val keySize = 256

    private val secretKey by lazy {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        if (keyStore.containsAlias(masterKeyAlias)) {
            keyStore.getKey(masterKeyAlias, null)
        } else {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore).let {
                it.init(
                    KeyGenParameterSpec.Builder(
                        masterKeyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(keySize)
                        .build()
                )
                it.generateKey()
            }
        }.let { keyStoreKey ->
            // Check if encrypt/decrypt is properly working (CP-1500).
            runCatching {
                val message = "message"
                val encrypted = encrypt(message, keyStoreKey)
                val decrypted = decrypt(encrypted, keyStoreKey)
                check(message == decrypted)
                keyStoreKey
            }.getOrElse {
                CoreLogger.e(LogTag.KEYSTORE_INIT, it)
                null
            }
        }
    }

    private fun encrypt(value: PlainByteArray, key: Key): EncryptedByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherByteArray = cipher.doFinal(value.array)
        return EncryptedByteArray(cipher.iv + cipherByteArray)
    }

    private fun decrypt(value: EncryptedByteArray, key: Key): PlainByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        val iv = value.array.copyOf(cipherIvBytes)
        val cipherByteArray = value.array.copyOfRange(cipherIvBytes, value.array.size)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(cipherGCMTagBits, iv))
        return PlainByteArray(cipher.doFinal(cipherByteArray))
    }

    private fun encrypt(value: String, key: Key): EncryptedString {
        return value.encodeToByteArray().use {
            Base64.encodeToString(encrypt(it, key).array, Base64.NO_WRAP)
        }
    }

    private fun decrypt(value: EncryptedString, key: Key): String {
        val encryptedByteArray = Base64.decode(value, Base64.NO_WRAP)
        return decrypt(EncryptedByteArray(encryptedByteArray), key).use {
            it.array.decodeToString()
        }
    }

    override fun isUsingKeyStore(): Boolean = secretKey != null

    override fun encrypt(value: PlainByteArray): EncryptedByteArray {
        return secretKey?.let { encrypt(value, it) } ?: EncryptedByteArray(value.array.copyOf())
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray {
        return secretKey?.let { decrypt(value, it) } ?: PlainByteArray(value.array.copyOf())
    }

    override fun encrypt(value: String): EncryptedString {
        return secretKey?.let { encrypt(value, it) } ?: value
    }

    override fun decrypt(value: EncryptedString): String {
        return secretKey?.let { decrypt(value, it) } ?: value
    }

    companion object {
        private const val DEFAULT_MASTER_KEY_ALIAS = "_me_proton_core_data_crypto_master_key_"

        /**
         * Default KeyStoreSimpleCrypto instance using master key alias.
         */
        val default by lazy { AndroidKeyStoreCrypto() }
    }
}
