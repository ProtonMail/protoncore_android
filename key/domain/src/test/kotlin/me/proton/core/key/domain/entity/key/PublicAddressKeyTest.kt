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

package me.proton.core.key.domain.entity.key

import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicAddressKeyTest {

    private val testPublicKey = PublicKey(
        key = "publicKey1",
        isPrimary = true,
        isActive = true,
        canEncrypt = true,
        canVerify = true
    )

    private val testPublicAddressKey = PublicAddressKey(
        email = "email@example.com",
        flags = 0,
        publicKey = testPublicKey
    )

    @Test
    fun `can encrypt returns false correctly`() {
        var result = testPublicAddressKey.canEncrypt()
        assertFalse(result)
        result = testPublicAddressKey.copy(flags = 1).canEncrypt()
        assertFalse(result)
        result = testPublicAddressKey.copy(flags = 4).canEncrypt()
        assertFalse(result)
    }

    @Test
    fun `can encrypt returns true correctly`() {
        var result = testPublicAddressKey.copy(flags = 6).canEncrypt()
        assertTrue(result)
        result = testPublicAddressKey.copy(flags = 7).canEncrypt()
        assertTrue(result)
    }

    @Test
    fun `can verify returns true correctly`() {
        var result = testPublicAddressKey.copy(flags = 13).canVerify()
        assertTrue(result)
        result = testPublicAddressKey.copy(flags = 15).canVerify()
        assertTrue(result)
        result = testPublicAddressKey.copy(flags = 1).canVerify()
        assertTrue(result)
    }

    @Test
    fun `can verify returns false correctly`() {
        var result = testPublicAddressKey.copy(flags = 14).canVerify()
        assertFalse(result)
        result = testPublicAddressKey.copy(flags = 2).canVerify()
        assertFalse(result)
    }

    @Test
    fun `can verify email returns correctly`() {
        val result = testPublicAddressKey.copy(flags = 1).canVerifyEmail()
        assertTrue(result)
    }

    @Test
    fun `can encrypt email returns false correctly`() {
        var result = testPublicAddressKey.copy(flags = 4).canEncryptEmail()
        assertFalse(result)
        result = testPublicAddressKey.copy(flags = 5).canEncryptEmail()
        assertFalse(result)
    }

    @Test
    fun `can encrypt email returns true correctly`() {
        var result = testPublicAddressKey.copy(flags = 3).canEncryptEmail()
        assertTrue(result)
        result = testPublicAddressKey.copy(flags = 2).canEncryptEmail()
        assertTrue(result)
    }
}