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

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.TestCryptoContext
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.toArmoredKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ArmoredKeyTest {

    @Test
    fun toArmoredKeyWithValidKey() {
        // GIVEN
        val context = object : TestCryptoContext() {
            override val pgpCrypto = object : TestPGPCrypto() {
                override fun isPrivateKey(key: Armored): Boolean = true
                override fun isValidKey(key: Armored): Boolean = true
            }
        }

        // WHEN
        val instance = "key".toArmoredKey(context, isPrimary = true, isActive = true, canEncrypt = true, canVerify = true)

        // THEN
        assertIs<ArmoredKey.Private>(instance)
    }

    @Test
    fun toArmoredKeyWithInvalidKey() {
        // GIVEN
        val context = object : TestCryptoContext() {
            override val pgpCrypto = object : TestPGPCrypto() {
                override fun isValidKey(key: Armored): Boolean = false
            }
        }

        // WHEN
        assertFailsWith(CryptoException::class) {
            "invalid key".toArmoredKey(context, isPrimary = true, isActive = true, canEncrypt = true, canVerify = true)
        }
    }

    @Test
    fun asPublicKeyWithPrivateKeyExtractsPublicKey() {
        // GIVEN
        val context = object : TestCryptoContext() {
            override val pgpCrypto = object : TestPGPCrypto() {
                override fun isValidKey(key: Armored): Boolean = true
                override fun isPrivateKey(key: Armored): Boolean = true
                override fun isPublicKey(key: Armored): Boolean = false
                override fun getPublicKey(privateKey: Armored): Armored = "public"
            }
        }

        // WHEN
        val publicKey = when (val instance = "someKey".toArmoredKey(context, isPrimary = true, isActive = true, canEncrypt = true, canVerify = true)) {
            is ArmoredKey.Private -> instance.key.publicKey(context)
            is ArmoredKey.Public -> instance.key
        }

        // THEN
        assertEquals("public", publicKey.key)
    }
}
