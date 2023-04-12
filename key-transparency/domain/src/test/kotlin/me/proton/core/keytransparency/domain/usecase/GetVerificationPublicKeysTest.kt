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

package me.proton.core.keytransparency.domain.usecase

import io.mockk.every
import io.mockk.mockk
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetVerificationPublicKeysTest {
    private lateinit var getVerificationPublicKeys: GetVerificationPublicKeys
    private val cryptoContext = mockk<CryptoContext>()

    @BeforeTest
    fun setUp() {
        getVerificationPublicKeys = GetVerificationPublicKeys(cryptoContext)
    }

    @Test
    fun `filters out inactive and compromised address keys`() {
        // given
        val key1 = "privatekey1"
        val key2 = "privatekey2"
        val key3 = "privatekey3"
        val userAddress = mockk<UserAddress> {
            every { keys } returns listOf(
                mockk {
                    every { privateKey.isPrimary } returns true
                    every { privateKey.isActive } returns true
                    every { privateKey.canVerify } returns true
                    every { privateKey.canEncrypt } returns true
                    every { privateKey.key } returns key1
                },
                mockk {
                    every { privateKey.isPrimary } returns false
                    every { privateKey.isActive } returns false
                    every { privateKey.canVerify } returns true
                    every { privateKey.canEncrypt } returns false
                    every { privateKey.key } returns key2
                },
                mockk {
                    every { privateKey.isPrimary } returns false
                    every { privateKey.isActive } returns true
                    every { privateKey.canVerify } returns false
                    every { privateKey.canEncrypt } returns false
                    every { privateKey.key } returns key3
                }
            )
        }
        val pubkey1 = "publickey1"
        every { cryptoContext.pgpCrypto.getPublicKey(key1) } returns pubkey1
        val expected = listOf(pubkey1)
        // when
        val publicKeys = getVerificationPublicKeys(userAddress)
        // then
        assertEquals(expected, publicKeys)
    }
}
