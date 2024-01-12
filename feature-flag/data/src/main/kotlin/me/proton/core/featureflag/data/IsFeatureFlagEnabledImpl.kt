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

package me.proton.core.featureflag.data

import android.content.Context
import androidx.annotation.BoolRes
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.IsFeatureFlagEnabled
import me.proton.core.featureflag.domain.entity.FeatureId

public abstract class IsFeatureFlagEnabledImpl(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager,
    private val featureId: FeatureId,
    @BoolRes private val localFlagId: Int
) : IsFeatureFlagEnabled {
    override fun invoke(userId: UserId?): Boolean {
        return isLocalEnabled() && isRemoteEnabled(userId)
    }

    override fun isLocalEnabled(): Boolean {
        return context.resources.getBoolean(localFlagId)
    }

    @OptIn(ExperimentalProtonFeatureFlag::class)
    override fun isRemoteEnabled(userId: UserId?): Boolean {
        return featureFlagManager.getValue(userId, featureId)
    }
}
