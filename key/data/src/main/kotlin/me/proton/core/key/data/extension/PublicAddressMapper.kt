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

package me.proton.core.key.data.extension

import me.proton.core.key.data.entity.PublicAddressEntity
import me.proton.core.key.data.entity.PublicAddressKeyEntity
import me.proton.core.key.data.entity.SignedKeyListEntity
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.entity.key.isCompromised
import me.proton.core.key.domain.entity.key.isObsolete

internal fun PublicSignedKeyList.toEntity() =
    SignedKeyListEntity(data = data, signature = signature)

internal fun PublicAddress.toEntity() = PublicAddressEntity(
    email = email,
    recipientType = recipientType,
    mimeType = mimeType,
    signedKeyListEntity = signedKeyList?.toEntity()
)

internal fun PublicAddressKey.toEntity() = PublicAddressKeyEntity(
    email = email,
    flags = flags,
    publicKey = publicKey.key,
    isPrimary = publicKey.isPrimary
)

internal fun List<PublicAddressKey>.toEntityList() = map { it.toEntity() }

internal fun SignedKeyListEntity.toPublicSignedKeyList() =
    PublicSignedKeyList(data = data, signature = signature)

internal fun PublicAddressEntity.toPublicAddress(keys: List<PublicAddressKeyEntity>) = PublicAddress(
    email = email,
    recipientType = recipientType,
    mimeType = mimeType,
    keys = keys.map { it.toPublicAddressKey() },
    signedKeyList = signedKeyListEntity?.toPublicSignedKeyList()
)

internal fun PublicAddressKeyEntity.toPublicAddressKey() = PublicAddressKey(
    email = email,
    flags = flags,
    publicKey = PublicKey(
        key = publicKey,
        isPrimary = isPrimary,
        isActive = true,
        canEncrypt = flags.isObsolete().not(),
        canVerify = flags.isCompromised().not()
    )
)
