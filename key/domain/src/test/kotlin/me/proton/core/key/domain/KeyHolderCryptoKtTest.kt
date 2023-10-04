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
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import org.junit.Assert.assertNotNull
import org.junit.Test

class KeyHolderCryptoKtTest {

    @Test
    fun `KeyHolderContext_generateNewHashKey`() {
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        val result = keyHolderContext.generateNewHashKey()
        assertNotNull(result)
        verify { pgpCrypto.generateNewHashKey() }
    }

    @Test
    fun `KeyHolderContext_getBase64Encoded`() {
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        val result = keyHolderContext.getBase64Encoded("message".toByteArray())
        assertNotNull(result)
        verify { pgpCrypto.getBase64Encoded("message".toByteArray()) }
    }

    @Test
    fun `KeyHolderContext_getBase64Decoded`() {
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        val result = keyHolderContext.getBase64Decoded("message")
        assertNotNull(result)
        verify { pgpCrypto.getBase64Decoded("message") }
    }

    @Test
    fun `KeyHolderContext_generateNestedPrivateKey`() {
        val username = "test-username"
        val domain = "test-domain"
        mockkObject(NestedPrivateKey)
        val nestedPrivateKey = mockk<NestedPrivateKey>(relaxed = true)
        every {
            NestedPrivateKey.Companion.generateNestedPrivateKey(
                any(),
                username,
                domain,
                any()
            )
        } returns nestedPrivateKey
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        val result = keyHolderContext.generateNestedPrivateKey(username, domain, mockk())
        assertNotNull(result)
        verify { NestedPrivateKey.generateNestedPrivateKey(context, username, domain, any()) }
    }

    @Test
    fun `KeyHolderContext_decryptData`() {
        mockkStatic("me.proton.core.key.domain.KeyHolderCryptoKt")
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        every { keyHolderContext.decryptSessionKey(any()) } returns SessionKey("session-key".toByteArray())
        val result = keyHolderContext.decryptData("data".toByteArray(), "key".toByteArray())
        assertNotNull(result)
        verify { keyHolderContext.decryptSessionKey("key".toByteArray()) }
        unmockkStatic("me.proton.core.key.domain.KeyHolderCryptoKt")
    }

    @Test
    fun `KeyHolderContext_decryptData sessionKey`() {
        mockkStatic("me.proton.core.key.domain.KeyHolderCryptoKt")
        val sessionKey = mockk<SessionKey>(relaxed = true)
        val context = mockk<TestCryptoContext>(relaxed = true)
        val pgpCrypto = mockk<TestCryptoContext.TestPGPCrypto>(relaxed = true)
        val keyHolderContext = mockk<KeyHolderContext>(relaxed = true)
        every { context.pgpCrypto } returns pgpCrypto
        every { keyHolderContext.context } returns context
        every { sessionKey.decryptData(context, any()) } returns "result".toByteArray()
        val result = keyHolderContext.decryptData("data".toByteArray(), sessionKey)
        assertNotNull(result)
        verify { sessionKey.decryptData(context, "data".toByteArray()) }
        unmockkStatic("me.proton.core.key.domain.KeyHolderCryptoKt")
    }
}