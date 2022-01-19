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
import me.proton.core.key.data.api.response.UserKeyResponse
import me.proton.core.key.data.api.response.UserResponse
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.user.data.entity.UserWithKeys
import me.proton.core.user.domain.entity.Delinquent
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.util.kotlin.toBooleanOrFalse

fun UserResponse.toUser(): User {
    val userId = UserId(id)
    return User(
        userId = userId,
        email = email,
        name = name,
        displayName = displayName,
        currency = currency,
        credit = credit,
        usedSpace = usedSpace,
        maxSpace = maxSpace,
        maxUpload = maxUpload,
        role = Role.map[role],
        private = private.toBooleanOrFalse(),
        subscribed = subscribed,
        services = services,
        delinquent = Delinquent.map[delinquent],
        keys = keys.map { it.toUserKey(userId) },
    )
}

internal fun UserKeyResponse.toUserKey(userId: UserId) = UserKey(
    userId = userId,
    version = version,
    activation = activation,
    active = active.toBooleanOrFalse(),
    keyId = KeyId(id),
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = primary.toBooleanOrFalse(),
        isActive = false,
        passphrase = null,
    )
)

internal fun User.toEntity(passphrase: EncryptedByteArray?) = UserEntity(
    userId = userId,
    email = email,
    name = name,
    displayName = displayName,
    currency = currency,
    credit = credit,
    usedSpace = usedSpace,
    maxSpace = maxSpace,
    maxUpload = maxUpload,
    role = role?.value,
    isPrivate = private,
    subscribed = subscribed,
    services = services,
    delinquent = delinquent?.value,
    passphrase = passphrase
)

internal fun UserKey.toEntity() = UserKeyEntity(
    userId = userId,
    keyId = keyId,
    version = version,
    privateKey = privateKey.key,
    isPrimary = privateKey.isPrimary,
    isUnlockable = privateKey.isActive,
    activation = activation,
    active = active
)

internal fun List<UserKey>.toEntityList() = map { it.toEntity() }

internal fun UserEntity.toUser(keys: List<UserKey>) = User(
    userId = userId,
    email = email,
    name = name,
    displayName = displayName,
    currency = currency,
    credit = credit,
    maxSpace = maxSpace,
    usedSpace = usedSpace,
    maxUpload = maxUpload,
    role = Role.map[role],
    private = isPrivate,
    subscribed = subscribed,
    services = services,
    delinquent = Delinquent.map[delinquent],
    keys = keys
)

@Suppress("UnnecessaryParentheses")
internal fun UserKeyEntity.toUserKey(passphrase: EncryptedByteArray?) = UserKey(
    userId = userId,
    keyId = keyId,
    version = version,
    activation = activation,
    active = active,
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = isPrimary,
        // If active is null (unknown during offline migration), we rely on isUnlockable.
        // active will be null until we refresh/update the UserKey from remote.
        isActive = (active ?: true) && isUnlockable && passphrase != null,
        passphrase = passphrase
    )
)

internal fun List<UserKeyEntity>.toUserKeyList(passphrase: EncryptedByteArray?) = map { it.toUserKey(passphrase) }

internal fun UserWithKeys.toUser() = entity.toUser(keys.toUserKeyList(entity.passphrase))
