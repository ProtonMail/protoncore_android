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

package me.proton.core.featureflag.data.testdata

import me.proton.core.featureflag.data.entity.FeatureFlagEntity
import me.proton.core.featureflag.data.remote.resource.FeatureResource
import me.proton.core.featureflag.data.testdata.UserIdTestData.userId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope

internal object FeatureFlagTestData {
    private const val RAW_FEATURE_ID = "featureId"
    private const val RAW_FEATURE_ID_1 = "featureId1"

    val featureId = FeatureId(RAW_FEATURE_ID)
    val featureId1 = FeatureId(RAW_FEATURE_ID_1)

    val enabledFeatureApiResponse = FeatureResource(
        featureId = featureId.id,
        isGlobal = false,
        defaultValue = true,
        value = true
    )

    val disabledFeatureApiResponse = FeatureResource(
        featureId = featureId1.id,
        isGlobal = false,
        defaultValue = false,
        value = false
    )

    val enabledFeature = FeatureFlag(
        featureId = featureId,
        value = true,
        userId = userId,
        scope = Scope.User,
        defaultValue = true
    )

    val disabledFeature = FeatureFlag(
        featureId = featureId1,
        value = false,
        userId = userId,
        scope = Scope.User,
        defaultValue = false
    )

    val enabledFeatureEntity = FeatureFlagEntity(
        userId = userId,
        featureId = featureId.id,
        scope = Scope.User,
        defaultValue = true,
        value = true
    )

    val disabledFeatureEntity = FeatureFlagEntity(
        userId = userId,
        featureId = featureId1.id,
        scope = Scope.User,
        defaultValue = false,
        value = false
    )
}
