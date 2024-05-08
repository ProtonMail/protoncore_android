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
import me.proton.core.key.data.api.response.UserResponse
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.data.entity.UserWithKeys
import me.proton.core.user.domain.entity.Delinquent
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.Type
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
        createdAtUtc = createTimeSeconds * 1_000L,
        usedSpace = usedSpace,
        maxSpace = maxSpace,
        maxUpload = maxUpload,
        type = Type.map[type],
        role = Role.map[role],
        private = private.toBooleanOrFalse(),
        subscribed = subscribed,
        services = services,
        delinquent = Delinquent.map[delinquent],
        recovery = recovery?.toUserRecovery(),
        keys = keys.map { it.toUserKey(userId) },
        maxBaseSpace = maxBaseSpace,
        maxDriveSpace = maxDriveSpace,
        usedBaseSpace = usedBaseSpace,
        usedDriveSpace = usedDriveSpace,
    )
}


internal fun User.toEntity(passphrase: EncryptedByteArray?) = UserEntity(
    userId = userId,
    email = email,
    name = name,
    displayName = displayName,
    currency = currency,
    credit = credit,
    createdAtUtc = createdAtUtc,
    usedSpace = usedSpace,
    maxSpace = maxSpace,
    maxUpload = maxUpload,
    type = type?.value,
    role = role?.value,
    isPrivate = private,
    subscribed = subscribed,
    services = services,
    delinquent = delinquent?.value,
    recovery = recovery?.toUserRecoveryEntity(),
    passphrase = passphrase,
    maxBaseSpace = maxBaseSpace,
    maxDriveSpace = maxDriveSpace,
    usedBaseSpace = usedBaseSpace,
    usedDriveSpace = usedDriveSpace,
)

internal fun UserEntity.toUser(keys: List<UserKey>) = User(
    userId = userId,
    email = email,
    name = name,
    displayName = displayName,
    currency = currency,
    credit = credit,
    createdAtUtc = createdAtUtc,
    maxSpace = maxSpace,
    usedSpace = usedSpace,
    maxUpload = maxUpload,
    type = Type.map[type],
    role = Role.map[role],
    private = isPrivate,
    subscribed = subscribed,
    services = services,
    delinquent = Delinquent.map[delinquent],
    recovery = recovery?.toUserRecovery(),
    keys = keys,
    maxBaseSpace = maxBaseSpace,
    maxDriveSpace = maxDriveSpace,
    usedBaseSpace = usedBaseSpace,
    usedDriveSpace = usedDriveSpace,
)

internal fun UserWithKeys.toUser() =
    entity.toUser(keys.toUserKeyList(entity.passphrase))
