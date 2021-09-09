/*
 * Copyright (c) 2021 Proton Technologies AG
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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.publicKey

fun KeyHolderContext.publicKeyRing(): PublicKeyRing =
    PublicKeyRing(privateKeyRing.keys.map { key -> key.publicKey(context) })

fun KeyHolder.publicKeyRing(context: CryptoContext): PublicKeyRing =
    PublicKeyRing(keys.map { it.privateKey.publicKey(context) })

fun PublicAddress.publicKeyRing(): PublicKeyRing = keys.publicKeyRing()
fun List<PublicAddressKey>.publicKeyRing(): PublicKeyRing = PublicKeyRing(map { it.publicKey })
fun PublicAddressKey.publicKeyRing(): PublicKeyRing = publicKey.publicKeyRing()
fun PublicKey.publicKeyRing(): PublicKeyRing = PublicKeyRing(listOf(this))
fun PrivateKey.publicKeyRing(context: CryptoContext): PublicKeyRing = PublicKeyRing(listOf(this.publicKey(context)))
