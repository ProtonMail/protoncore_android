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

package me.proton.core.usersettings.data.extension

import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.data.api.response.OrganizationKeysResponse
import me.proton.core.usersettings.data.api.response.OrganizationResponse
import me.proton.core.usersettings.data.entity.OrganizationEntity
import me.proton.core.usersettings.data.entity.OrganizationKeysEntity
import me.proton.core.usersettings.domain.entity.Organization
import me.proton.core.usersettings.domain.entity.OrganizationKeys

internal fun OrganizationResponse.fromResponse(userId: UserId) = Organization(
    userId = userId,
    name = name,
    displayName = displayName,
    planName = planName,
    twoFactorGracePeriod = twoFactorGracePeriod,
    theme = theme,
    email = email,
    maxDomains = maxDomains,
    maxAddresses = maxAddresses,
    maxSpace = maxSpace,
    maxMembers = maxMembers,
    maxVPN = maxVPN,
    maxCalendars = maxCalendars,
    features = features,
    flags = flags,
    usedDomains = usedDomains,
    usedAddresses = usedAddresses,
    usedSpace = usedSpace,
    assignedSpace = assignedSpace,
    usedMembers = usedMembers,
    usedVPN = usedVPN,
    usedCalendars = usedCalendars,
    hasKeys = hasKeys,
    toMigrate = toMigrate
)

internal fun OrganizationKeysResponse.fromResponse(userId: UserId) = OrganizationKeys(
    userId = userId,
    publicKey = publicKey,
    privateKey = privateKey
)

internal fun OrganizationEntity.fromEntity() = Organization(
    userId = userId,
    name = name,
    displayName = displayName,
    planName = planName,
    twoFactorGracePeriod = twoFactorGracePeriod,
    theme = theme,
    email = email,
    maxDomains = maxDomains,
    maxAddresses = maxAddresses,
    maxSpace = maxSpace,
    maxMembers = maxMembers,
    maxVPN = maxVPN,
    maxCalendars = maxCalendars,
    features = features,
    flags = flags,
    usedDomains = usedDomains,
    usedAddresses = usedAddresses,
    usedSpace = usedSpace,
    assignedSpace = assignedSpace,
    usedMembers = usedMembers,
    usedVPN = usedVPN,
    usedCalendars = usedCalendars,
    hasKeys = hasKeys,
    toMigrate = toMigrate
)

internal fun OrganizationKeysEntity.fromEntity() = OrganizationKeys(
    userId = userId,
    publicKey = publicKey,
    privateKey = privateKey
)

internal fun Organization.toEntity() = OrganizationEntity(
    userId = userId,
    name = name,
    displayName = displayName,
    planName = planName,
    twoFactorGracePeriod = twoFactorGracePeriod,
    theme = theme,
    email = email,
    maxDomains = maxDomains,
    maxAddresses = maxAddresses,
    maxSpace = maxSpace,
    maxMembers = maxMembers,
    maxVPN = maxVPN,
    maxCalendars = maxCalendars,
    features = features,
    flags = flags,
    usedDomains = usedDomains,
    usedAddresses = usedAddresses,
    usedSpace = usedSpace,
    assignedSpace = assignedSpace,
    usedMembers = usedMembers,
    usedVPN = usedVPN,
    usedCalendars = usedCalendars,
    hasKeys = hasKeys,
    toMigrate = toMigrate
)

internal fun OrganizationKeys.toEntity() = OrganizationKeysEntity(
    userId = userId,
    publicKey = publicKey,
    privateKey = privateKey
)
