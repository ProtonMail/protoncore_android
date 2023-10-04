/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.key.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.ArmoredKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArmoredCryptoKtTest {

    private val context = spyk<TestCryptoContext>()

    @Test
    fun `Armored_toArmoredKey public key`() {
        val armored = "armored"
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { pgpCrypto.isPrivateKey(armored) } returns false
        every { pgpCrypto.isPublicKey(armored) } returns true
        val result = armored.toArmoredKey(context,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
        assertIs<ArmoredKey.Public>(result)
    }

    @Test
    fun `Armored_toPublicKey default`() {
        val armored = "armored"
        val result = armored.toPublicKey()
        assertTrue(result.canEncrypt)
        assertTrue(result.isActive)
        assertTrue(result.canVerify)
        assertTrue(result.isPrimary)
    }

    @Test
    fun `Armored_toPublicKey`() {
        val armored = "armored"
        val result = armored.toPublicKey(
            isPrimary = false,
            isActive = false,
            canEncrypt = false,
            canVerify = false
        )
        assertFalse(result.canEncrypt)
        assertFalse(result.isActive)
        assertFalse(result.canVerify)
        assertFalse(result.isPrimary)
    }

    @Test
    fun `Armored_toPrivateKey default`() {
        val armored = "armored"
        val result = armored.toPrivateKey()
        assertTrue(result.canEncrypt)
        assertTrue(result.isActive)
        assertTrue(result.canVerify)
        assertTrue(result.isPrimary)
        assertNull(result.passphrase)
    }

    @Test
    fun `Armored_toPrivateKey`() {
        val armored = "armored"
        val result = armored.toPrivateKey(
            isPrimary = false,
            isActive = false,
            canEncrypt = false,
            canVerify = false,
            passphrase = EncryptedByteArray("passphrase".toByteArray())
        )
        assertFalse(result.canEncrypt)
        assertFalse(result.isActive)
        assertFalse(result.canVerify)
        assertFalse(result.isPrimary)
        assertEquals(EncryptedByteArray("passphrase".toByteArray()), result.passphrase)
    }
}