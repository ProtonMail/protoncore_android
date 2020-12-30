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

package me.proton.core.crypto.android.simple

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import me.proton.core.crypto.common.simple.EncryptedByteArray
import me.proton.core.crypto.common.simple.EncryptedString
import me.proton.core.crypto.common.simple.PlainByteArray
import me.proton.core.crypto.common.simple.SimpleCrypto
import me.proton.core.crypto.common.simple.use
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * [SimpleCrypto] implementation based on Android KeyStore System.
 *
 * @see <a href="https://developer.android.com/training/articles/keystore">Android KeyStore System</a>
 * @see [KeyStore]
 * @see [KeyGenParameterSpec]
 */
class KeyStoreSimpleCrypto private constructor(
    masterKeyAlias: String = DEFAULT_MASTER_KEY_ALIAS
) : SimpleCrypto {

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
        }
    }

    override fun encrypt(value: PlainByteArray): EncryptedByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cipherByteArray = cipher.doFinal(value.array)
        return EncryptedByteArray(cipher.iv + cipherByteArray)
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray {
        val cipher = Cipher.getInstance(cipherTransformation)
        val iv = value.array.copyOf(cipherIvBytes)
        val cipherByteArray = value.array.copyOfRange(cipherIvBytes, value.array.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(cipherGCMTagBits, iv))
        return PlainByteArray(cipher.doFinal(cipherByteArray))
    }

    override fun encrypt(value: String): EncryptedString {
        return value.encodeToByteArray().use {
            Base64.encodeToString(encrypt(it).array, Base64.NO_WRAP)
        }
    }

    override fun decrypt(value: EncryptedString): String {
        val encryptedByteArray = Base64.decode(value, Base64.NO_WRAP)
        return decrypt(EncryptedByteArray(encryptedByteArray)).use {
            it.array.decodeToString()
        }
    }

    companion object {
        private const val DEFAULT_MASTER_KEY_ALIAS = "_me_proton_core_data_crypto_master_key_"

        /**
         * Default KeyStoreSimpleCrypto instance using master key alias.
         */
        val default by lazy { KeyStoreSimpleCrypto() }
    }
}
