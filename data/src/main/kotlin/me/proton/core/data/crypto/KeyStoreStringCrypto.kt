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

package me.proton.core.data.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * [StringCrypto] implementation base on Android [MasterKey] and [EncryptedSharedPreferences].
 */
class KeyStoreStringCrypto(
    context: Context,
    masterKeyAlias: String = DEFAULT_MASTER_KEY_ALIAS
) : StringCrypto {

    private val encryptedSharedPrefFileName = "${KeyStoreStringCrypto::class.java.name}-encryptedSharedPref"
    private val secretPrefKey = "${KeyStoreStringCrypto::class.java.name}-secretKey"

    private val cipherTransformation = "AES/GCM/NoPadding"
    private val cipherIvBytes = 12
    private val keyGeneratorAlgorithm = "AES"
    private val keySize = 256

    private val secureRandom by lazy {
        SecureRandom()
    }

    private val masterKey by lazy {
        MasterKey.Builder(context, masterKeyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            encryptedSharedPrefFileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val secretKey by lazy {
        val secretKeyBase64 = encryptedSharedPreferences.getString(secretPrefKey, null) ?: generateSecretKeyBase64()
        val secretKeyByteArray = Base64.decode(secretKeyBase64, Base64.NO_WRAP)
        SecretKeySpec(secretKeyByteArray, keyGeneratorAlgorithm)
    }

    private fun generateSecretKeyBase64(): String {
        val generator = KeyGenerator.getInstance(keyGeneratorAlgorithm).apply { init(keySize, secureRandom) }
        val secretKey = generator.generateKey()
        val secretKeyBase64 = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        encryptedSharedPreferences.edit().putString(secretPrefKey, secretKeyBase64).apply()
        return secretKeyBase64
    }

    override fun encrypt(value: String): EncryptedString {
        val unencryptedByteArray = value.encodeToByteArray()
        val cipher = Cipher.getInstance(cipherTransformation)
        val iv = ByteArray(cipherIvBytes)
        secureRandom.nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherByteArray = cipher.doFinal(unencryptedByteArray)
        val encryptedByteArray = iv + cipherByteArray
        return EncryptedString(Base64.encodeToString(encryptedByteArray, Base64.NO_WRAP))
    }

    override fun decrypt(value: EncryptedString): String {
        val encryptedByteArray = Base64.decode(value.encrypted, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(cipherTransformation)
        val iv = Arrays.copyOf(encryptedByteArray, cipherIvBytes)
        val cipherByteArray = Arrays.copyOfRange(encryptedByteArray, cipherIvBytes, encryptedByteArray.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val unencryptedByteArray = cipher.doFinal(cipherByteArray)
        return unencryptedByteArray.decodeToString()
    }

    companion object {
        private const val DEFAULT_MASTER_KEY_ALIAS = "_me_proton_core_data_crypto_master_key_"
    }
}
