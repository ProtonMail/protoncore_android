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

package me.proton.core.keytransparency.data.local

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.key.data.api.response.SignedKeyListResponse
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.data.local.entity.AddressChangeEntity
import me.proton.core.keytransparency.domain.entity.AddressChange

internal fun AddressChange.toEntity(keyStoreCrypto: KeyStoreCrypto): AddressChangeEntity {
    return AddressChangeEntity(
        userId = userId,
        changeId = changeId,
        counterEncrypted = counter.toString().encrypt(keyStoreCrypto),
        emailEncrypted = email.encrypt(keyStoreCrypto),
        epochIdEncrypted = epochId.toString().encrypt(keyStoreCrypto),
        creationTimestampEncrypted = creationTimestamp.toString().encrypt(keyStoreCrypto),
        publicKeysEncrypted = publicKeys.map { it.encrypt(keyStoreCrypto) },
        isObsolete = isObsolete.toString().encrypt(keyStoreCrypto)
    )
}

internal fun AddressChangeEntity.toAddressChange(keyStoreCrypto: KeyStoreCrypto): AddressChange {
    return AddressChange(
        userId = userId,
        changeId = changeId,
        counter = counterEncrypted.decrypt(keyStoreCrypto).toInt(),
        email = emailEncrypted.decrypt(keyStoreCrypto),
        epochId = epochIdEncrypted.decrypt(keyStoreCrypto).toInt(),
        creationTimestamp = creationTimestampEncrypted.decrypt(keyStoreCrypto).toLong(),
        publicKeys = publicKeysEncrypted.map { it.decrypt(keyStoreCrypto) },
        isObsolete = isObsolete.decrypt(keyStoreCrypto).toBooleanStrict()
    )
}

internal fun SignedKeyListResponse.toPublicSignedKeyList() = PublicSignedKeyList(
    data = data,
    signature = signature,
    minEpochId = minEpochId,
    maxEpochId = maxEpochId,
    expectedMinEpochId = expectedMinEpochId
)
