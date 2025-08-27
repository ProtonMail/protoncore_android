/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.featureflag.data.local

import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId

// If FeatureFlag.userId is null -> assume it's for all users / global.
private val globalUserId = UserId("global")
internal fun UserId?.orGlobal() = this ?: globalUserId
internal fun UserId?.withGlobal() = listOfNotNull(this, globalUserId)

internal fun FeatureFlag.toEntity() = FeatureFlagEntity(
    userId = userId.orGlobal(),
    featureId = featureId.id,
    scope = scope,
    defaultValue = defaultValue,
    value = value,
    variantName = variantName,
    payloadType = payloadType,
    payloadValue = payloadValue,
)

internal fun FeatureFlagEntity.toFeatureFlag() = FeatureFlag(
    userId = if (userId == globalUserId) null else userId,
    featureId = FeatureId(featureId),
    scope = scope,
    defaultValue = defaultValue,
    value = value,
    variantName = variantName,
    payloadType = payloadType,
    payloadValue = payloadValue,
)
