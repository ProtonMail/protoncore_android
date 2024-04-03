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

package me.proton.core.key.domain.extension

import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

/**
 * Create a [KeyHolder] instance based on [PrivateKey].
 *
 * @throws [IllegalStateException] if there is no valid passphrase for the [PrivateKey].
 */
fun PrivateKey.keyHolder(keyId: KeyId = KeyId.unused): KeyHolder {
    checkNotNull(passphrase) { "No valid passphrase for private key." }
    return object : KeyHolder {
        override val keys: List<KeyHolderPrivateKey> = listOf(object : KeyHolderPrivateKey {
            override val keyId: KeyId = keyId
            override val privateKey: PrivateKey = this@keyHolder
        })
    }
}
