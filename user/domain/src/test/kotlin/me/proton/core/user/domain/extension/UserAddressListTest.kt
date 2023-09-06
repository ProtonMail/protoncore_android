/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.user.domain.extension

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Test
import kotlin.test.assertEquals

class UserAddressListTest {

    private val addressKey = mockk<UserAddressKey> {
        every { token } returns "token"
        every { signature } returns "signature"
    }

    private val addressKeyNotMigrated = mockk<UserAddressKey> {
        every { token } returns null
        every { signature } returns null
    }

    private val addressPrimary = mockk<UserAddress> {
        every { type } returns AddressType.Original
        every { order } returns 0
        every { keys } returns listOf(addressKey)
    }

    private val addressNoKey = mockk<UserAddress> {
        every { type } returns AddressType.Custom
        every { order } returns 1
        every { keys } returns emptyList()
    }

    private val addressNotMigrated = mockk<UserAddress> {
        every { type } returns AddressType.Original
        every { order } returns 1
        every { keys } returns listOf(addressKeyNotMigrated)
    }

    private val addressPremium = mockk<UserAddress> {
        every { type } returns AddressType.Premium
        every { order } returns 1
        every { keys } returns listOf(addressKey)
    }

    private val addressExternal = mockk<UserAddress> {
        every { type } returns AddressType.External
        every { order } returns 1
        every { keys } returns listOf(addressKey)
    }

    @Test
    fun primary() = runTest {
        assertEquals(
            expected = addressPrimary,
            actual = listOf(addressPrimary, addressPremium).primary()
        )
    }

    @Test
    fun sorted() = runTest {
        assertEquals(
            expected = listOf(addressPrimary, addressPremium),
            actual = listOf(addressPremium, addressPrimary).sorted()
        )
    }

    @Test
    fun firstInternalOrNull() = runTest {
        assertEquals(
            expected = addressPrimary,
            actual = listOf(addressExternal, addressPrimary).firstInternalOrNull()
        )
        assertEquals(
            expected = null,
            actual = listOf(addressExternal).firstInternalOrNull()
        )
    }

    @Test
    fun hasMigratedKey() = runTest {
        assertEquals(
            expected = true,
            actual = listOf(addressPrimary, addressNotMigrated).hasMigratedKey()
        )
        assertEquals(
            expected = false,
            actual = listOf(addressNotMigrated).hasMigratedKey()
        )
    }

    @Test
    fun generateNewKeyFormat() = runTest {
        assertEquals(
            expected = true,
            actual = listOf(addressPrimary, addressNotMigrated).generateNewKeyFormat()
        )
        assertEquals(
            expected = true,
            actual = listOf(addressNoKey).generateNewKeyFormat()
        )
        assertEquals(
            expected = false,
            actual = listOf(addressNotMigrated, addressNoKey).generateNewKeyFormat()
        )
    }

    @Test
    fun hasInternalAddressKey() = runTest {
        assertEquals(
            expected = true,
            actual = listOf(addressExternal, addressPrimary).hasInternalAddressKey()
        )
        assertEquals(
            expected = false,
            actual = listOf(addressExternal, addressNoKey).hasInternalAddressKey()
        )
    }

    @Test
    fun hasOriginalAddress() = runTest {
        assertEquals(
            expected = true,
            actual = listOf(addressExternal, addressPrimary).hasOriginalAddress()
        )
        assertEquals(
            expected = false,
            actual = listOf(addressExternal, addressPremium).hasOriginalAddress()
        )
        assertEquals(
            expected = false,
            actual = emptyList<UserAddress>().hasOriginalAddress()
        )
    }

    @Test
    fun filterExternal() = runTest {
        assertEquals(
            expected = listOf(addressExternal),
            actual = listOf(addressExternal, addressPrimary).filterExternal()
        )
        assertEquals(
            expected = listOf(addressExternal),
            actual = listOf(addressExternal, addressNoKey).filterExternal()
        )
        assertEquals(
            expected = emptyList(),
            actual = listOf(addressPrimary, addressPremium, addressNoKey).filterExternal()
        )
    }

    @Test
    fun filterInternal() = runTest {
        assertEquals(
            expected = listOf(addressPrimary),
            actual = listOf(addressExternal, addressPrimary).filterInternal()
        )
        assertEquals(
            expected = listOf(addressNoKey),
            actual = listOf(addressExternal, addressNoKey).filterInternal()
        )
        assertEquals(
            expected = listOf(addressPrimary, addressPremium, addressNoKey),
            actual = listOf(addressPrimary, addressPremium, addressNoKey).filterInternal()
        )
    }

    @Test
    fun hasMissingKeys() = runTest {
        assertEquals(
            expected = false,
            actual = listOf(addressExternal, addressPrimary).hasMissingKeys()
        )
        assertEquals(
            expected = true,
            actual = listOf(addressExternal, addressNoKey).hasMissingKeys()
        )
        assertEquals(
            expected = false,
            actual = listOf(addressPrimary, addressPremium).hasMissingKeys()
        )
    }

    @Test
    fun filterHasNoKeys() = runTest {
        assertEquals(
            expected = emptyList(),
            actual = listOf(addressPrimary, addressExternal).filterHasNoKeys()
        )
        assertEquals(
            expected = listOf(addressNoKey),
            actual = listOf(addressPrimary, addressNoKey).filterHasNoKeys()
        )
    }
}
