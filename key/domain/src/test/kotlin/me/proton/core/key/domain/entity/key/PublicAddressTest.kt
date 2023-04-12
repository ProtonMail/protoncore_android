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

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicAddressTest {
    // test data
    private val publicKey = PublicKey(
        key = "publicKey1",
        isPrimary = true,
        isActive = true,
        canEncrypt = true,
        canVerify = true
    )

    private val publicAddressKey = PublicAddressKey(
        email = "email@example.com",
        flags = 0,
        publicKey = publicKey
    )

    private val testPublicAddress = PublicAddress(
        email = "email@example.com",
        mimeType = null,
        recipientType = 0,
        keys = listOf(publicAddressKey),
        signedKeyList = null,
        ignoreKT = 0
    )

    @Test
    fun `public addresses can encrypt returns correctly`() {
        var result = testPublicAddress.canEncryptEmail()
        assertFalse(result)
        result = testPublicAddress.copy(keys = listOf(publicAddressKey.copy(flags = 3))).canEncryptEmail()
        assertTrue(result)
        result = testPublicAddress.copy(keys = listOf(publicAddressKey.copy(flags = 5))).canEncryptEmail()
        assertFalse(result)
        result = testPublicAddress.copy(keys = listOf(publicAddressKey.copy(flags = 2))).canEncryptEmail()
        assertTrue(result)
    }

    @Test
    fun `external address returns false`() {
        var result = testPublicAddress.canVerifyEmail()
        assertFalse(result)
        result = testPublicAddress.copy(keys = listOf(publicAddressKey.copy(flags = 9))).canVerifyEmail()
        assertFalse(result)
        result = testPublicAddress.copy(keys = listOf(publicAddressKey.copy(flags = 1))).canVerifyEmail()
        assertTrue(result)
    }
}