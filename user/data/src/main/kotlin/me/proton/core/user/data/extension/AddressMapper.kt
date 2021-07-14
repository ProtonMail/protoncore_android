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

package me.proton.core.user.data.extension

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.response.AddressKeyResponse
import me.proton.core.key.data.api.response.AddressResponse
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.AddressKeyEntity
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.extension.canEncrypt
import me.proton.core.user.domain.extension.canVerify
import me.proton.core.util.kotlin.toBooleanOrFalse

fun AddressResponse.toAddress(userId: UserId) = toAddress(userId, passphrase = null)

internal fun AddressResponse.toAddress(userId: UserId, passphrase: EncryptedByteArray?): UserAddress {
    val addressId = AddressId(id)
    return UserAddress(
        userId = userId,
        addressId = addressId,
        email = email,
        displayName = displayName,
        signature = signature,
        domainId = domainId,
        canSend = send.toBooleanOrFalse(),
        canReceive = receive.toBooleanOrFalse(),
        enabled = status.toBooleanOrFalse(),
        type = AddressType.map[type],
        order = order,
        keys = keys?.map { it.toUserAddressKey(addressId, passphrase) }.orEmpty(),
    )
}

internal fun AddressKeyResponse.toUserAddressKey(
    addressId: AddressId,
    passphrase: EncryptedByteArray?
) = UserAddressKey(
    addressId = addressId,
    version = version,
    flags = flags,
    token = token,
    signature = signature,
    activation = activation,
    active = active.toBooleanOrFalse(),
    keyId = KeyId(id),
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = primary.toBooleanOrFalse(),
        passphrase = passphrase
    )
)

internal fun AddressResponse.toEntity(userId: UserId) = AddressEntity(
    userId = userId,
    addressId = AddressId(id),
    email = email,
    displayName = displayName,
    signature = signature,
    domainId = domainId,
    canSend = send.toBooleanOrFalse(),
    canReceive = receive.toBooleanOrFalse(),
    enabled = status.toBooleanOrFalse(),
    type = type,
    order = order
)

internal fun AddressKeyResponse.toEntity(addressId: AddressId) = AddressKeyEntity(
    addressId = addressId,
    keyId = KeyId(id),
    version = version,
    privateKey = privateKey,
    isPrimary = primary.toBooleanOrFalse(),
    flags = flags,
    token = token,
    signature = signature,
    fingerprint = fingerprint,
    fingerprints = fingerprints,
    activation = activation,
    active = active.toBooleanOrFalse()
)

internal fun AddressEntity.toUserAddress(keys: List<UserAddressKey>) = UserAddress(
    userId = userId,
    addressId = addressId,
    email = email,
    displayName = displayName,
    signature = signature,
    domainId = domainId,
    canSend = canSend,
    canReceive = canReceive,
    enabled = enabled,
    type = AddressType.map[type],
    order = order,
    keys = keys
)

fun List<AddressKeyResponse>.toEntityList(addressId: AddressId) = map { it.toEntity(addressId) }

internal fun AddressKeyEntity.toUserAddressKey(passphrase: EncryptedByteArray?) = UserAddressKey(
    addressId = addressId,
    version = version,
    flags = flags,
    token = token,
    signature = signature,
    activation = activation,
    active = active,
    keyId = keyId,
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = isPrimary,
        isActive = active,
        canEncrypt = flags.canEncrypt(),
        canVerify = flags.canVerify(),
        passphrase = passphrase
    )
)

internal fun UserAddress.toEntity() = AddressEntity(
    userId = userId,
    addressId = addressId,
    email = email,
    displayName = displayName,
    signature = signature,
    domainId = domainId,
    canSend = canSend,
    canReceive = canReceive,
    enabled = enabled,
    type = type?.value,
    order = order
)

internal fun UserAddressKey.toEntity() = AddressKeyEntity(
    addressId = addressId,
    keyId = keyId,
    version = version,
    privateKey = privateKey.key,
    isPrimary = privateKey.isPrimary,
    flags = flags,
    token = token,
    signature = signature,
    activation = activation,
    active = active
)

internal fun List<UserAddressKey>.toEntityList() = map { it.toEntity() }
