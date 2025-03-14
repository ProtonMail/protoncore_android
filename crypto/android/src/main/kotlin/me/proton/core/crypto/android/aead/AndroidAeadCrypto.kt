/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.crypto.android.aead

import android.util.Base64
import me.proton.core.crypto.common.aead.AeadCrypto
import me.proton.core.crypto.common.aead.AeadEncryptedByteArray
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.use
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * [AeadCrypto] implementation based on Javax [Cipher].
 *
 * Default: AES/GCM/NoPadding, 12 bytes IV, 16 bytes GCM tag.
 */
class AndroidAeadCrypto internal constructor(
    private val cipherFactory: () -> Cipher,
    private val keyAlgorithm: () -> String,
    private val authTagBits: Int,
    private val ivBytes: Int
) : AeadCrypto {

    private fun getRandomIv() = ByteArray(ivBytes).apply { SecureRandom().nextBytes(this) }

    private fun getKey(key: ByteArray) = SecretKeySpec(key, keyAlgorithm.invoke())

    private fun encrypt(
        value: PlainByteArray,
        key: Key,
        aad: ByteArray? = null
    ): AeadEncryptedByteArray {
        val cipher = cipherFactory.invoke()
        val iv = getRandomIv()
        val gcmSpec = GCMParameterSpec(authTagBits, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        if (aad != null) cipher.updateAAD(aad)
        val encryptedData = cipher.doFinal(value.array)
        return AeadEncryptedByteArray(iv + encryptedData)
    }

    private fun decrypt(
        value: AeadEncryptedByteArray,
        key: Key,
        aad: ByteArray? = null
    ): PlainByteArray {
        val cipher = cipherFactory.invoke()
        val gcmSpec = GCMParameterSpec(authTagBits, value.array, 0, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        if (aad != null) cipher.updateAAD(aad)
        val inputSize = value.array.size - ivBytes
        val decryptedData = cipher.doFinal(value.array, ivBytes, inputSize)
        return PlainByteArray(decryptedData)
    }

    private fun encrypt(
        value: String,
        key: Key,
        aad: ByteArray? = null
    ): AeadEncryptedString = value.encodeToByteArray().use {
        Base64.encodeToString(encrypt(value = it, key = key, aad = aad).array, Base64.NO_WRAP)
    }

    private fun decrypt(
        value: EncryptedString,
        key: Key,
        aad: ByteArray? = null
    ): String = decrypt(
        value = AeadEncryptedByteArray(Base64.decode(value, Base64.NO_WRAP)),
        key = key,
        aad = aad
    ).use { it.array.decodeToString() }

    override fun encrypt(
        value: String,
        key: ByteArray,
        aad: ByteArray?
    ): AeadEncryptedString {
        return encrypt(value = value, key = getKey(key), aad = aad)
    }

    override fun encrypt(
        value: PlainByteArray,
        key: ByteArray,
        aad: ByteArray?
    ): AeadEncryptedByteArray {
        return encrypt(value = value, key = getKey(key), aad = aad)
    }

    override fun decrypt(
        value: AeadEncryptedString,
        key: ByteArray,
        aad: ByteArray?
    ): String {
        return decrypt(value = value, key = getKey(key), aad = aad)
    }

    override fun decrypt(
        value: AeadEncryptedByteArray,
        key: ByteArray,
        aad: ByteArray?
    ): PlainByteArray {
        return decrypt(value = value, key = getKey(key), aad = aad)
    }
}
