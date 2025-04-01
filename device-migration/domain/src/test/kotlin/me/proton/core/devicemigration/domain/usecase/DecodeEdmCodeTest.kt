/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DecodeEdmCodeTest {
    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto
    private lateinit var tested: DecodeEdmCode

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { keyStoreCrypto.encrypt(any<PlainByteArray>()) } answers {
            EncryptedByteArray(firstArg<PlainByteArray>().array)
        }
        tested = DecodeEdmCode(keyStoreCrypto)
    }

    @Test
    fun `decode invalid string`() {
        assertNull(tested(""))
        assertNull(tested("   "))
        assertNull(tested("::"))
        assertNull(tested("0:::"))
        assertNull(tested("0: : : "))
        assertNull(tested(":::"))
        assertNull(tested("::::"))
        assertNull(tested("UserCode:EncryptionKey:ChildClientID"))
        assertNull(tested("0:UserCode:EncryptionKey:ChildClientID"))
        assertNull(tested("0::RW5jcnlwdGlvbktleQ==:ChildClientID"))
        assertNull(tested("0:UserCode::ChildClientID"))
        assertNull(tested("0:UserCode:RW5jcnlwdGlvbktleQ==:"))
        assertNull(tested("0:UserCode:RW5jcnlwdGlvbktleQ:ChildClientID")) // base64 padding missing
        assertNull(tested("  0::RW5jcnlwdGlvbktleQ==:  "))
    }

    @Test
    fun `decode valid string`() {
        val params = tested("0:UserCode:RW5jcnlwdGlvbktleQ==:ChildClientID")
        assertNotNull(params)
        assertEquals("ChildClientID", params.childClientId.value)
        assertContentEquals("EncryptionKey".encodeToByteArray(), params.encryptionKey.value.array)
        assertEquals("UserCode", params.userCode.value)
    }

    @Test
    fun `decode valid string with extra`() {
        val params = tested("0:UserCode:RW5jcnlwdGlvbktleQ==:ChildClientID:ExtraParam")
        assertNotNull(params)
        assertEquals("ChildClientID", params.childClientId.value)
        assertContentEquals("EncryptionKey".encodeToByteArray(), params.encryptionKey.value.array)
        assertEquals("UserCode", params.userCode.value)
    }
}
