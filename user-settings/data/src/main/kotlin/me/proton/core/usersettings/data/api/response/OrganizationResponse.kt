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

package me.proton.core.usersettings.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationResponse(
    @SerialName("Name")
    val name: String,
    @SerialName("DisplayName")
    val displayName: String?,
    @SerialName("PlanName")
    val planName: String?,
    @SerialName("VPNPlanName")
    val vpnPlanName: String?,
    @SerialName("TwoFactorGracePeriod")
    val twoFactorGracePeriod: Int?,
    @SerialName("Theme")
    val theme: String?,
    @SerialName("Email")
    val email: String?,
    @SerialName("MaxDomains")
    val maxDomains: Int,
    @SerialName("MaxAddresses")
    val maxAddresses: Int,
    @SerialName("MaxSpace")
    val maxSpace: Long,
    @SerialName("MaxMembers")
    val maxMembers: Int,
    @SerialName("MaxVPN")
    val maxVPN: Int?,
    @SerialName("MaxCalendars")
    val maxCalendars: Int?,
    @SerialName("Features")
    val features: Int,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("UsedDomains")
    val usedDomains: Int,
    @SerialName("UsedAddresses")
    val usedAddresses: Int,
    @SerialName("UsedSpace")
    val usedSpace: Long,
    @SerialName("AssignedSpace")
    val assignedSpace: Long,
    @SerialName("UsedMembers")
    val usedMembers: Int,
    @SerialName("UsedVPN")
    val usedVPN: Int?,
    @SerialName("UsedCalendars")
    val usedCalendars: Int?,
    @SerialName("HasKeys")
    val hasKeys: Int,
    @SerialName("ToMigrate")
    val toMigrate: Int
)
