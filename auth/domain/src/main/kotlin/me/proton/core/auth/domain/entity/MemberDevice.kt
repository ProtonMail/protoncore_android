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

package me.proton.core.auth.domain.entity

import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UniqueId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

data class MemberDeviceId(override val id: String) : UniqueId

/**
 * Member device is similar to [AuthDevice], but represents the device of a member of an organization.
 * @param userId The ID of the admin (which should be currently logged in).
 * @param memberId The ID of the member user.
 */
data class MemberDevice(
    val userId: UserId,
    val deviceId: MemberDeviceId,
    val memberId: UserId,
    val addressId: AddressId?,
    val state: AuthDeviceState,
    val name: String,
    val localizedClientName: String,
    val platform: String?,
    val createdAtUtcSeconds: Long,
    val activatedAtUtcSeconds: Long?,
    val rejectedAtUtcSeconds: Long?,
    val activationToken: EncryptedMessage?,
    val lastActivityAtUtcSeconds: Long
)
