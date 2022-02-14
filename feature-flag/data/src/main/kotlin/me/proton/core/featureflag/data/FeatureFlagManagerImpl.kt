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

package me.proton.core.featureflag.data

import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import javax.inject.Inject

class FeatureFlagManagerImpl @Inject constructor(
    val repository: FeatureFlagRepository
) : FeatureFlagManager {

    override fun observe(userId: UserId, featureId: FeatureId) = repository.observe(userId, featureId)

    override suspend fun get(userId: UserId, featureId: FeatureId) = repository.get(userId, featureId)

    override suspend fun prefetch(userId: UserId, featureIds: List<FeatureId>) = repository.prefetch(userId, featureIds)
}
