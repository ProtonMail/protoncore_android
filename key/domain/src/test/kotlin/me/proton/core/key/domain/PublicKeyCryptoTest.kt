/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.entity.key.PublicKey
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PublicKeyCryptoTest {

    private val unlockedKey: UnlockedKey = object : UnlockedKey {
        override val value = "Unarmored".toByteArray()
        override fun close() = Unit
    }

    private val publicKey = PublicKey(
        key = "publicKey",
        isPrimary = true,
        isActive = true,
        canEncrypt = true,
        canVerify = true
    )

    private val publicKey2 = PublicKey(
        key = "publicKey2",
        isPrimary = true,
        isActive = true,
        canEncrypt = false,
        canVerify = false
    )

    private val context = TestCryptoContext().also {
        it.unlockedKeys[publicKey.key] = unlockedKey.value
        it.unlockedKeys[publicKey2.key] = unlockedKey.value
    }

    private val message = "message"

    @Test
    fun publicKey_encryptText() {
        val encryptedText = publicKey.encryptText(context, message)
        assertNotNull(encryptedText)
    }

    @Test(expected = CryptoException::class)
    fun publicKey_encryptText_canEncrypt_false() {
        publicKey2.encryptText(context, message)
    }

    @Test
    fun publicKey_verifyText() {
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val verified = publicKey.verifyText(context, message, signature)
        assertTrue(verified)
    }

    @Test
    fun publicKey_verifyText_canVerify_false() {
        val signature = context.pgpCrypto.signText(message, unlockedKey.value)
        val verified = publicKey2.verifyText(context, message, signature)
        assertFalse(verified)
    }
}
