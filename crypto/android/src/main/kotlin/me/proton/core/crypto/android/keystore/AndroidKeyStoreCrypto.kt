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
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * [KeyStoreCrypto] implementation based on Android KeyStore System.
 *
 * Note: This implementation use the same KeyStore workaround as Tink `AndroidKeystoreAesGcm`.
 *
 * @see <a href="https://developer.android.com/training/articles/keystore">Android KeyStore System</a>
 * @see [KeyStore]
 * @see [KeyGenParameterSpec]
 * @see <a href="https://github.com/google/tink/blob/master/java_src/src/main/java/com/google/crypto/tink/integration/android/AndroidKeystoreAesGcm.java">Tink AndroidKeystoreAesGcm</a>
 */
@Suppress("MemberVisibilityCanBePrivate")
class AndroidKeyStoreCrypto internal constructor(
    private val masterKeyAlias: String,
    private val keyStoreFactory: () -> KeyStore,
    private val keyGeneratorFactory: () -> KeyGenerator,
    private val cipherFactory: () -> Cipher,
) : KeyStoreCrypto {

    @Volatile
    private var secretKeyInitialized = false

    @Volatile
    private var secretKey: Key? = null

    internal fun clearKeySync() {
        synchronized(lock) {
            secretKey = null
            secretKeyInitialized = false
        }
    }

    internal fun getSecretKeySync(): Key? {
        synchronized(lock) {
            if (!secretKeyInitialized) setSecretKeySync(initKey())
            return secretKey
        }
    }

    internal fun setSecretKeySync(key: Key?) {
        synchronized(lock) {
            secretKey = key
            secretKeyInitialized = true
        }
    }

    internal fun initKey(): Key? {
        val keyStore = keyStoreFactory.invoke().apply { load(null) }
        val key = getKeyOrRetryOrNull(keyStore) ?: generateNewKey(keyStore)
        return if (isUsableKey(key)) key else null
    }

    internal fun getKey(keyStore: KeyStore): Key? = when {
        keyStore.containsAlias(masterKeyAlias) -> keyStore.getKey(masterKeyAlias, null)
        else -> null
    }

    internal fun getKeyOrRetryOrNull(keyStore: KeyStore): Key? {
        return runCatching { runOrRetryOnce(LogTag.KEYSTORE_INIT_RETRY) { getKey(keyStore) } }.getOrNull()
    }

    internal fun generateNewKey(keyStore: KeyStore): Key {
        if (keyStore.containsAlias(masterKeyAlias)) {
            keyStore.deleteEntry(masterKeyAlias).also {
                CoreLogger.i(LogTag.KEYSTORE_INIT_DELETE_KEY, "Deleted '$masterKeyAlias' entry from this keystore.")
            }
        }
        return keyGeneratorFactory.invoke().run {
            init(
                KeyGenParameterSpec
                    .Builder(masterKeyAlias, keyPurpose)
                    .setBlockModes(keyBlockMode)
                    .setEncryptionPaddings(keyEncryptionPadding)
                    .setKeySize(keySize)
                    .build()
            )
            generateKey().also {
                CoreLogger.i(LogTag.KEYSTORE_INIT_ADD_KEY, "Added '$masterKeyAlias' entry in this keystore.")
            }
        }
    }

    internal fun isUsableKey(key: Key): Boolean = runCatching {
        val message = "message"
        // Check if encrypt/decrypt is properly working (CP-1500).
        val encrypted = encryptOrRetry(message, key)
        val decrypted = decryptOrRetry(encrypted, key)
        check(message == decrypted)
        true
    }.getOrElse {
        CoreLogger.e(LogTag.KEYSTORE_INIT, it)
        false
    }

    internal fun encryptInternal(value: PlainByteArray, key: Key): EncryptedByteArray {
        val cipher = cipherFactory.invoke()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherByteArray = cipher.doFinal(value.array)
        return EncryptedByteArray(cipher.iv + cipherByteArray)
    }

    internal fun decryptInternal(value: EncryptedByteArray, key: Key): PlainByteArray {
        val cipher = cipherFactory.invoke()
        val iv = value.array.copyOf(cipherIvBytes)
        val cipherByteArray = value.array.copyOfRange(cipherIvBytes, value.array.size)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(cipherGCMTagBits, iv))
        return PlainByteArray(cipher.doFinal(cipherByteArray))
    }

    private fun encryptOrRetry(value: PlainByteArray, key: Key): EncryptedByteArray {
        return runOrRetryOnce(LogTag.KEYSTORE_ENCRYPT_RETRY) { encryptInternal(value, key) }
    }

    private fun decryptOrRetry(value: EncryptedByteArray, key: Key): PlainByteArray {
        return runOrRetryOnce(LogTag.KEYSTORE_DECRYPT_RETRY) { decryptInternal(value, key) }
    }

    private fun encryptOrRetry(value: String, key: Key): EncryptedString {
        return value.encodeToByteArray().use {
            Base64.encodeToString(encryptOrRetry(it, key).array, Base64.NO_WRAP)
        }
    }

    private fun decryptOrRetry(value: EncryptedString, key: Key): String {
        val encryptedByteArray = Base64.decode(value, Base64.NO_WRAP)
        return decryptOrRetry(EncryptedByteArray(encryptedByteArray), key).use {
            it.array.decodeToString()
        }
    }

    internal fun <T> logAndRetry(logTag: String, error: Throwable, block: () -> T): T {
        CoreLogger.e(logTag, error)
        sleep()
        return block()
    }

    private fun <T> runOrRetryOnce(logTag: String, block: () -> T): T {
        return try {
            block()
        } catch (error: ProviderException) {
            logAndRetry(logTag, error, block)
        } catch (error: GeneralSecurityException) {
            logAndRetry(logTag, error, block)
        }
    }

    private fun sleep() {
        try {
            Thread.sleep((Math.random() * maxWaitTimeMillisecondsBeforeRetry).toLong())
        } catch (e: InterruptedException) {
            // Ignored.
        }
    }

    override fun isUsingKeyStore(): Boolean = getSecretKeySync() != null

    override fun encrypt(value: PlainByteArray): EncryptedByteArray {
        return getSecretKeySync()?.let { encryptOrRetry(value, it) } ?: EncryptedByteArray(value.array.copyOf())
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray {
        return getSecretKeySync()?.let { decryptOrRetry(value, it) } ?: PlainByteArray(value.array.copyOf())
    }

    override fun encrypt(value: String): EncryptedString {
        return getSecretKeySync()?.let { encryptOrRetry(value, it) } ?: value
    }

    override fun decrypt(value: EncryptedString): String {
        return getSecretKeySync()?.let { decryptOrRetry(value, it) } ?: value
    }

    companion object {
        private const val androidKeyStore = "AndroidKeyStore"
        private const val keyAlgorithm = KeyProperties.KEY_ALGORITHM_AES
        private const val keyBlockMode = KeyProperties.BLOCK_MODE_GCM
        private const val keyPurpose = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        private const val keyEncryptionPadding = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val keySize = 256
        private const val cipherTransformation = "AES/GCM/NoPadding"
        private const val cipherIvBytes = 12
        private const val cipherGCMTagBits = 128

        private const val defaultMasterKeyAlias = "_me_proton_core_data_crypto_master_key_"
        private const val maxWaitTimeMillisecondsBeforeRetry = 100.0
        private val lock = Object()

        /**
         * Default KeyStoreSimpleCrypto instance using master key alias.
         */
        val default by lazy {
            AndroidKeyStoreCrypto(
                masterKeyAlias = defaultMasterKeyAlias,
                keyStoreFactory = { KeyStore.getInstance(androidKeyStore) },
                keyGeneratorFactory = { KeyGenerator.getInstance(keyAlgorithm, androidKeyStore) },
                cipherFactory = { Cipher.getInstance(cipherTransformation) },
            )
        }
    }
}
