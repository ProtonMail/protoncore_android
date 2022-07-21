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

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.decryptOrElse
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.test.android.runBlockingWithTimeout
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testing expected success cases with the real [KeyStore] implementation.
 */
internal class AndroidKeyStoreCryptoAndroidTest {

    private lateinit var crypto: AndroidKeyStoreCrypto

    @Before
    fun setup() {
        crypto = spyk(AndroidKeyStoreCrypto.default)
    }

    @Test
    fun isUsingKeyStoreThenEncryptAndDecryptWorks() {
        // GIVEN
        assertTrue(crypto.isUsingKeyStore())

        // WHEN
        val data = "testing"
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun encryptDecryptBenchmarkAverageTimeIsBelow30Millis() = runBlockingWithTimeout(3_000) {
        // GIVEN
        val repeat = 100
        val data = "testing"

        // WHEN
        val totalTimeMillis = (1..repeat).fold(0L) { acc, _ ->
            acc + measureTimeMillis {
                val encrypted = data.encrypt(crypto)
                val decrypted = encrypted.decrypt(crypto)
                assertEquals(expected = data, actual = decrypted)
            }
        }

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        assertTrue((totalTimeMillis / repeat) < 30)
    }

    @Test
    fun whenIsUsableKeyReturnFalseThenEncryptAndDecryptDoNothing() {
        // GIVEN
        every { crypto.isUsableKey(any()) } returns false

        // WHEN
        val data = "testing"
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertFalse(crypto.isUsingKeyStore())
        assertEquals(expected = data, actual = encrypted)
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenGetKeyReturnNullThenGenerateNewKey() {
        // GIVEN
        every { crypto.getKey(any()) } returns null

        // WHEN
        val data = "testing"
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        verify(exactly = 1) { crypto.getKey(any()) }
        verify(exactly = 1) { crypto.generateNewKey(any()) }
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenGenerateNewKeyThenPreviousEncryptedDataThrowException() {
        // GIVEN
        val dataWithFirstKey = "testing with first key"
        val encryptedWithFirstKey = dataWithFirstKey.encrypt(crypto)
        val decryptedWithFirstKey = encryptedWithFirstKey.decrypt(crypto)
        assertTrue(crypto.isUsingKeyStore())
        assertEquals(expected = dataWithFirstKey, actual = decryptedWithFirstKey)

        // WHEN
        every { crypto.getKey(any()) } throws GeneralSecurityException("Key not available.")

        // Force re-initialization.
        crypto.clearKeySync()

        val dataWithSecondKey = "testing with second key"
        val encryptedWithSecondKey = dataWithSecondKey.encrypt(crypto)
        val decryptedWithSecondKey = encryptedWithSecondKey.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        verify(exactly = 3) { crypto.getKey(any()) }
        verify(exactly = 1) { crypto.generateNewKey(any()) }
        verify(exactly = 1) { crypto.logAndRetry(any(), any(), any()) }
        assertEquals(expected = dataWithSecondKey, actual = decryptedWithSecondKey)

        // We can't decrypt encrypted data coming from the first key anymore.
        assertFailsWith<GeneralSecurityException> {
            encryptedWithFirstKey.decrypt(crypto)
        }
        verify(exactly = 2) { crypto.logAndRetry(any(), any(), any()) }

        val failure = "failure"
        val decryptFailure = encryptedWithFirstKey.decryptOrElse(crypto) { failure }
        assertEquals(expected = failure, actual = decryptFailure)
        verify(exactly = 3) { crypto.logAndRetry(any(), any(), any()) }
    }

    @Test
    fun whenEncryptDecryptEmptyString() {
        // GIVEN
        val empty = ""

        // WHEN
        val encrypted = empty.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        assertEquals(expected = empty, actual = decrypted)
    }

    @Test
    fun whenEncryptDecryptEmptyByteArray() {
        // GIVEN
        byteArrayOf().use { empty ->
            // WHEN
            val encrypted = empty.encrypt(crypto)
            val decrypted = encrypted.decrypt(crypto)

            // THEN
            assertTrue(crypto.isUsingKeyStore())
            assertContentEquals(expected = empty.array, actual = decrypted.array)
        }
    }

    @Test
    fun whenEncryptDecrypt71CharsUsingLatin1String() {
        // GIVEN
        val data = "ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾"

        // WHEN
        val encrypted = data.encrypt(crypto)
        val decrypted = encrypted.decrypt(crypto)

        // THEN
        assertTrue(crypto.isUsingKeyStore())
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenEncryptDecrypt100KByteArray() {
        // GIVEN
        Random.nextBytes(100 * 1000).use { data ->
            // WHEN
            val encrypted = data.encrypt(crypto)
            val decrypted = encrypted.decrypt(crypto)

            // THEN
            assertTrue(crypto.isUsingKeyStore())
            assertContentEquals(expected = data.array, actual = decrypted.array)
        }
    }
}
