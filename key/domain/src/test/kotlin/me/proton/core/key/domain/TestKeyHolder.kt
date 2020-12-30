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

import me.proton.core.crypto.common.simple.PlainByteArray
import me.proton.core.crypto.common.simple.encrypt
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

class TestKeyHolder(
    private val name: String,
    private val context: TestCryptoContext,
    private val keyCount: Int = 4,
    private val passphraseCount: Int = 4
) : KeyHolder {

    inner class TestKeyHolderPrivateKey(id: String, isPrimary: Boolean, passphrase: ByteArray?) : KeyHolderPrivateKey {
        override val keyId: KeyId = KeyId(id)
        private val unlockedKey = "$name#key$id".toByteArray()
        override val privateKey: PrivateKey = PrivateKey(
            // Encrypt the key with passphrase.
            key = unlockedKey.encrypt(passphrase ?: unlockedKey).fromByteArray(),
            isPrimary = isPrimary,
            // Encrypt passphrase as it should be stored in PrivateKey.
            passphrase = passphrase?.let { PlainByteArray(passphrase).encrypt(context.simpleCrypto) }
        ).also { key ->
            // Workaround: Remember unlockedKey to be able to decrypt messages encrypted with PublicKey.
            val publicKey = context.pgpCrypto.getPublicKey(key.key)
            context.unlockedKeys[publicKey] = unlockedKey
        }
    }

    override val keys: List<KeyHolderPrivateKey> =
        (1..keyCount).asSequence()
            .map { index ->
                TestKeyHolderPrivateKey(
                    id = "$index",
                    isPrimary = index == 1,
                    passphrase = "$name#passphrase$index".toByteArray().takeIf { index <= passphraseCount }
                )
            }
            .toList()
}
