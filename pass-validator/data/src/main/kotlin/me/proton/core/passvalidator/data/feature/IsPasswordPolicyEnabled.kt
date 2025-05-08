/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.passvalidator.data.feature

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.passvalidator.data.R
import javax.inject.Inject

internal class IsPasswordPolicyEnabled @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureFlagManager: FeatureFlagManager,
) {
    operator fun invoke(userId: UserId?): Boolean = isLocalEnabled() && !isRemoteDisabled(userId)

    private fun isLocalEnabled() = context.resources.getBoolean(R.bool.core_feature_password_policy_enabled)

    @OptIn(ExperimentalProtonFeatureFlag::class)
    private fun isRemoteDisabled(userId: UserId?) = featureFlagManager.getValue(
        userId,
        FeatureId("PasswordPolicyDisabled")
    )
}
