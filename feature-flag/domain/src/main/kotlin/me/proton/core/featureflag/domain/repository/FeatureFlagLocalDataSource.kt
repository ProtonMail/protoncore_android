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

package me.proton.core.featureflag.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope

public interface FeatureFlagLocalDataSource {
    public suspend fun getAll(scope: Scope): List<FeatureFlag>
    public suspend fun replaceAll(userId: UserId?, scope: Scope, flags: List<FeatureFlag>)
    public fun observe(userId: UserId?, featureIds: Set<FeatureId>): Flow<List<FeatureFlag>>
    public suspend fun upsert(flags: List<FeatureFlag>)
    public suspend fun updateValue(userId: UserId?, featureId: FeatureId, value: Boolean)
}
