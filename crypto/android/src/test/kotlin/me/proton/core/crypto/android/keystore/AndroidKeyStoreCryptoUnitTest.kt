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
import android.util.Base64
import io.mockk.MockKStubScope
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.verify
import me.proton.core.crypto.common.keystore.LogTag
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testing expected failure cases with a mocked [KeyStore].
 */
internal class AndroidKeyStoreCryptoUnitTest {

    private val data = "testing"

    private val keyStore = mockk<KeyStore>(relaxed = true) {
        every { load(any()) } returns Unit
        every { containsAlias(any()) } returns true
        every { getKey(any(), any()) } returns mockk()
    }

    private val keyGenerator = mockk<KeyGenerator>(relaxed = true) {
        every { generateKey() } returns mockk()
    }

    private val cipher = mockk<Cipher>(relaxed = true) {
        every { iv } returns byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        every { doFinal(any()) } answers { firstArg() } // no encryption
    }

    private val logger = mockk<Logger>(relaxed = true)

    private lateinit var crypto: AndroidKeyStoreCrypto

    @Suppress("UNCHECKED_CAST")
    fun <T, B> MockKStubScope<T, B>.answersSelf() = answers { self as T }

    @Before
    fun setup() {
        CoreLogger.set(logger)

        mockkConstructor(KeyGenParameterSpec.Builder::class)
        every { anyConstructed<KeyGenParameterSpec.Builder>().setBlockModes(any()) }.answersSelf()
        every { anyConstructed<KeyGenParameterSpec.Builder>().setEncryptionPaddings(any()) }.answersSelf()
        every { anyConstructed<KeyGenParameterSpec.Builder>().setKeySize(any()) }.answersSelf()
        every { anyConstructed<KeyGenParameterSpec.Builder>().build() } returns mockk()

        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers { firstArg<ByteArray>().decodeToString() }
        every { Base64.decode(any<String>(), any()) } answers { firstArg<String>().encodeToByteArray() }

        crypto = AndroidKeyStoreCrypto(
            masterKeyAlias = "masterKeyAlias",
            keyStoreFactory = { keyStore },
            keyGeneratorFactory = { keyGenerator },
            cipherFactory = { cipher }
        )
    }

    @After
    fun afterEveryTest() {
        unmockkConstructor(KeyGenParameterSpec.Builder::class)
    }

    @Test
    fun whenKeyStoreContainsAliasReturnFalseThenGenerateKey() {
        // GIVEN
        every { keyStore.containsAlias(any()) } returns false

        // WHEN
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        verify(Ordering.ORDERED) {
            // Return false.
            keyStore.containsAlias(any())
            // Before generateKey.
            keyStore.containsAlias(any())
            keyGenerator.generateKey()
            logger.i(LogTag.KEYSTORE_INIT_ADD_KEY, any())
        }
        verify(exactly = 0) { keyStore.getKey(any(), any()) }
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenKeyStoreGetKeyThrowGeneralSecurityExceptionThenRetryOnceAndGenerateNewKey() {
        // GIVEN
        every { keyStore.getKey(any(), any()) } throws GeneralSecurityException("Key not available.")

        // WHEN
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        verify(Ordering.ORDERED) {
            // First try.
            keyStore.containsAlias(any())
            keyStore.getKey(any(), any())
            logger.e(LogTag.KEYSTORE_INIT_RETRY, any())
            // Retry.
            keyStore.containsAlias(any())
            keyStore.getKey(any(), any())
            // Before generateKey.
            keyStore.containsAlias(any())
            keyStore.deleteEntry(any())
            logger.i(LogTag.KEYSTORE_INIT_DELETE_KEY, any())
            keyGenerator.generateKey()
            logger.i(LogTag.KEYSTORE_INIT_ADD_KEY, any())
        }
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenCipherEncryptThrowGeneralSecurityExceptionThenRetryOnce() {
        // GIVEN
        assertTrue(crypto.isUsingKeyStore())

        // WHEN
        every { cipher.doFinal(any()) } throws GeneralSecurityException("Encrypt not available.")
        assertFailsWith<GeneralSecurityException> { data.encrypt(crypto) }

        // THEN
        verify(Ordering.ORDERED) {
            // Init -> encrypt/decrypt success.
            cipher.doFinal(any())
            cipher.doFinal(any())
            // Encrypt try.
            cipher.doFinal(any())
            logger.e(LogTag.KEYSTORE_ENCRYPT_RETRY, any())
            // Encrypt retry.
            cipher.doFinal(any())
        }
    }

    @Test
    fun whenCipherDecryptThrowGeneralSecurityExceptionThenRetryOnceAndThenFail() {
        // GIVEN
        assertTrue(crypto.isUsingKeyStore())
        val encrypted = data.encrypt(crypto)

        // WHEN
        every { cipher.doFinal(any()) } throws GeneralSecurityException("Encrypt not available.")
        assertFailsWith<GeneralSecurityException> { encrypted.decrypt(crypto) }

        // THEN
        verify(Ordering.ORDERED) {
            // Init -> encrypt/decrypt success.
            cipher.doFinal(any())
            cipher.doFinal(any())
            // Encrypt -> success.
            cipher.doFinal(any())
            // Decrypt -> error.
            cipher.doFinal(any())
            logger.e(LogTag.KEYSTORE_DECRYPT_RETRY, any())
            // Decrypt retry.
            cipher.doFinal(any())
        }
    }

    @Test
    fun whenCipherDecryptThrowGeneralSecurityExceptionThenRetryOnceAndThenSucceed() {
        // GIVEN
        assertTrue(crypto.isUsingKeyStore())
        val encrypted = data.encrypt(crypto)

        // WHEN
        every { cipher.doFinal(any()) }
            .throws(GeneralSecurityException("Decrypt not available."))
            .andThen(data.toByteArray())

        val decrypted = encrypted.decrypt(crypto)

        // THEN
        verify(Ordering.ORDERED) {
            // Init -> encrypt/decrypt success.
            cipher.doFinal(any())
            cipher.doFinal(any())
            // Encrypt -> success.
            cipher.doFinal(any())
            // Decrypt -> error.
            cipher.doFinal(any())
            logger.e(LogTag.KEYSTORE_DECRYPT_RETRY, any())
            // Decrypt retry -> success.
            cipher.doFinal(any())
        }
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenCipherThrowGeneralSecurityExceptionDuringInitialization() {
        // GIVEN
        every { cipher.doFinal(any()) } throws GeneralSecurityException("Encrypt not available.")

        // WHEN
        assertFalse(crypto.isUsingKeyStore())

        // THEN
        verify(Ordering.ORDERED) {
            // First try.
            cipher.doFinal(any())
            logger.e(LogTag.KEYSTORE_ENCRYPT_RETRY, any())

            // Retry.
            cipher.doFinal(any())
            logger.e(LogTag.KEYSTORE_INIT, any())
        }
    }
}
