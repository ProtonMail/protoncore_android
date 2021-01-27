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

package me.proton.core.key.domain.entity.key

import me.proton.core.crypto.common.pgp.Armored

data class PrivateAddressKey(
    val keyId: KeyId,
    val flags: Int,
    val privateKey: PrivateKey,
    val version: Int,
    val token: Armored? = null,
    val signature: Armored? = null,
    val fingerprint: String? = null,
    val fingerprints: List<String>? = null,
    val activation: Armored? = null,
    val active: Boolean
)
