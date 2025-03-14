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

import io.mockk.spyk
import me.proton.core.crypto.common.aead.decrypt
import me.proton.core.crypto.common.aead.encrypt
import me.proton.core.crypto.common.keystore.use
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException
import java.security.SecureRandom
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Testing expected success cases with the real [AndroidAeadCrypto] implementation.
 */
internal class AndroidAeadCryptoAndroidTest {

    private lateinit var crypto: AndroidAeadCrypto

    private fun getRandomByte(size: Int = 32) =
        ByteArray(size).apply { SecureRandom().nextBytes(this) }

    @Before
    fun setup() {
        crypto = spyk(AndroidAeadCryptoFactory.default)
    }

    @Test
    fun encryptAndDecryptWorks() {
        // GIVEN
        val data = "testing"
        val key = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key)
        val decrypted = encrypted.decrypt(crypto, key)

        // THEN
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun encryptAndDecryptDoNotWorks() {
        // GIVEN
        val data = "testing"
        val key = getRandomByte()
        val key2 = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key)

        // THEN
        assertFailsWith<GeneralSecurityException> {
            encrypted.decrypt(crypto, key2)
        }
    }

    @Test
    fun whenEncryptDecryptEmptyString() {
        // GIVEN
        val data = ""
        val key = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key)
        val decrypted = encrypted.decrypt(crypto, key)

        // THEN
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenEncryptDecryptEmptyByteArray() {
        // GIVEN
        val key = getRandomByte()

        byteArrayOf().use { empty ->
            // WHEN
            val encrypted = empty.encrypt(crypto, key)
            val decrypted = encrypted.decrypt(crypto, key)

            // THEN
            assertContentEquals(expected = empty.array, actual = decrypted.array)
        }
    }

    @Test
    fun whenEncryptDecryptUseAadWorks() {
        // GIVEN
        val data = ""
        val key = getRandomByte()
        val aad = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key, aad)
        val decrypted = encrypted.decrypt(crypto, key, aad)

        // THEN
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenEncryptDecryptUseAadDoNotWorks() {
        // GIVEN
        val data = ""
        val key = getRandomByte()
        val aad = getRandomByte()
        val aad2 = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key, aad)

        // THEN
        assertFailsWith<GeneralSecurityException> {
            encrypted.decrypt(crypto, key, aad2)
        }
    }

    @Test
    fun whenEncryptDecrypt71CharsUsingLatin1String() {
        // GIVEN
        val data = "ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾ÀÁÂÃÄÅÆ¼½¾"
        val key = getRandomByte()

        // WHEN
        val encrypted = data.encrypt(crypto, key)
        val decrypted = encrypted.decrypt(crypto, key)

        // THEN
        assertEquals(expected = data, actual = decrypted)
    }

    @Test
    fun whenEncryptDecrypt100KByteArray() {
        // GIVEN
        val key = getRandomByte()

        getRandomByte(100 * 1000).use { data ->
            // WHEN
            val encrypted = data.encrypt(crypto, key)
            val decrypted = encrypted.decrypt(crypto, key)

            // THEN
            assertContentEquals(expected = data.array, actual = decrypted.array)
        }
    }
}
