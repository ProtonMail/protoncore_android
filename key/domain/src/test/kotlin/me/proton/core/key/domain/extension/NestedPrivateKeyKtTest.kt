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

package me.proton.core.key.domain.extension

import io.mockk.every
import io.mockk.mockk
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.entity.key.PrivateKey
import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull


class NestedPrivateKeyKtTest {

    @Test
    fun `NestedPrivateKey_keyHolder default unused key`() {
        val nestedPrivateKey = mockk<NestedPrivateKey>(relaxed = true)
        val privateKey = mockk<PrivateKey>(relaxed = true)
        val passphrase = EncryptedByteArray("passphrase".toByteArray())
        every { nestedPrivateKey.privateKey } returns privateKey
        every { privateKey.passphrase } returns passphrase
        val result = nestedPrivateKey.keyHolder()

        assertNotNull(result)
        assertEquals(1, result.keys.size)
        assertEquals(KeyId.unused, result.keys[0].keyId)
    }

    @Test
    fun `NestedPrivateKey_keyHolder`() {
        val nestedPrivateKey = mockk<NestedPrivateKey>(relaxed = true)
        val privateKey = mockk<PrivateKey>(relaxed = true)
        val passphrase = EncryptedByteArray("passphrase".toByteArray())
        every { nestedPrivateKey.privateKey } returns privateKey
        every { privateKey.passphrase } returns passphrase
        val result = nestedPrivateKey.keyHolder(KeyId("test-key-id"))

        assertNotNull(result)
        assertEquals(1, result.keys.size)
        assertEquals(KeyId("test-key-id"), result.keys[0].keyId)
    }

    @Test
    fun `NestedPrivateKey_keyHolder null passphrase`() {
        val nestedPrivateKey = mockk<NestedPrivateKey>(relaxed = true)
        val privateKey = mockk<PrivateKey>(relaxed = true)
        every { nestedPrivateKey.privateKey } returns privateKey
        every { privateKey.passphrase } returns null
        assertFailsWith<IllegalStateException> {
            nestedPrivateKey.keyHolder()
        }
    }
}