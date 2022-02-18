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

package me.proton.core.usersettings.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

@Entity(
    primaryKeys = ["userId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrganizationEntity(
    val userId: UserId,
    val name: String,
    val displayName: String?,
    val planName: String?,
    val vpnPlanName: String?,
    val twoFactorGracePeriod: Int?,
    val theme: String?,
    val email: String?,
    val maxDomains: Int?,
    val maxAddresses: Int?,
    val maxSpace: Long?,
    val maxMembers: Int?,
    val maxVPN: Int?,
    val maxCalendars: Int?,
    val features: Int?,
    val flags: Int?,
    val usedDomains: Int?,
    val usedAddresses: Int?,
    val usedSpace: Long?,
    val assignedSpace: Long?,
    val usedMembers: Int?,
    val usedVPN: Int?,
    val usedCalendars: Int?,
    val hasKeys: Int?,
    val toMigrate: Int?
)
