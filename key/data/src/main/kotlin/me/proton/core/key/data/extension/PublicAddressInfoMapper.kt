/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.key.data.extension

import me.proton.core.key.data.entity.PublicAddressKeyDataEntity
import me.proton.core.key.data.entity.PublicAddressInfoWithKeys
import me.proton.core.key.data.entity.PublicAddressInfoEntity
import me.proton.core.key.data.entity.EmailAddressType
import me.proton.core.key.domain.entity.key.PublicAddressInfo
import me.proton.core.key.domain.entity.key.PublicAddressKeyData
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicAddressKeySource
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.canEncrypt
import me.proton.core.key.domain.entity.key.canVerify

fun PublicAddressInfoEntity.toPublicAddressInfo(
    addressKeyEntities: List<PublicAddressKeyDataEntity>
): PublicAddressInfo = PublicAddressInfo(
    email = email,
    address = PublicAddressKeyData(
        keys = addressKeyEntities
            .filter { it.emailAddressType == EmailAddressType.REGULAR }
            .map { it.toPublicAddressKey() },
        signedKeyList = addressSignedKeyList?.toPublicSignedKeyList()
    ),
    catchAll = PublicAddressKeyData(
        keys = addressKeyEntities
            .filter { it.emailAddressType == EmailAddressType.CATCH_ALL }
            .map { it.toPublicAddressKey() },
        signedKeyList = catchAllSignedKeyList?.toPublicSignedKeyList()
    ),
    unverified = PublicAddressKeyData(
        keys = addressKeyEntities
            .filter { it.emailAddressType == EmailAddressType.UNVERIFIED }
            .map { it.toPublicAddressKey() },
        signedKeyList = null
    ),
    warnings = warnings,
    protonMx = protonMx,
    isProton = isProton
)

fun PublicAddressInfo.toEntity(): PublicAddressInfoWithKeys = PublicAddressInfoWithKeys(
    entity = toPublicAddressInfoEntity(),
    keys = address.keys.map { it.toPublicAddressKeyDataEntity(EmailAddressType.REGULAR) } +
            catchAll?.keys.orEmpty().map { it.toPublicAddressKeyDataEntity(EmailAddressType.CATCH_ALL) } +
            unverified?.keys.orEmpty().map { it.toPublicAddressKeyDataEntity(EmailAddressType.UNVERIFIED) }
)

fun PublicAddressInfo.toPublicAddressInfoEntity() = PublicAddressInfoEntity(
    email = email,
    warnings = warnings,
    protonMx = protonMx,
    isProton = isProton,
    addressSignedKeyList = address.signedKeyList?.toEntity(),
    catchAllSignedKeyList = catchAll?.signedKeyList?.toEntity()
)

fun PublicAddressKeyDataEntity.toPublicAddressKey(): PublicAddressKey = PublicAddressKey(
    email = email,
    flags = flags,
    publicKey = PublicKey(
        key = publicKey,
        isPrimary = isPrimary,
        isActive = true,
        canEncrypt = flags.canEncrypt(),
        canVerify = flags.canVerify()
    ),
    source = PublicAddressKeySource.fromCode(source)
)

fun PublicAddressKey.toPublicAddressKeyDataEntity(emailAddressType: Int) = PublicAddressKeyDataEntity(
    email = email,
    emailAddressType = emailAddressType,
    flags = flags,
    publicKey = publicKey.key,
    isPrimary = publicKey.isPrimary,
    source = source?.code
)
