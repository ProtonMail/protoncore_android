/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.payment.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId

import me.proton.core.featureflag.data.IsFeatureFlagEnabledImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.payment.domain.features.IsMobileUpgradesEnabled
import javax.inject.Inject

public class IsMobileUpgradesEnabledImpl @Inject constructor(
    @ApplicationContext context: Context,
    featureFlagManager: FeatureFlagManager
) : IsMobileUpgradesEnabled, IsFeatureFlagEnabledImpl(
    context = context,
    featureFlagManager = featureFlagManager,
    featureId = FeatureId("MobileUpgrades"),
    localFlagId = R.bool.core_feature_mobile_upgrades_enabled
) {
    override fun isRemoteEnabled(userId: UserId?): Boolean {
        return false
    }
}
