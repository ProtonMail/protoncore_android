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

/**
 * Repository to access [FeatureFlag]s which are local or remote
 */
public interface FeatureFlagRepository {

    /**
     * Observe a feature flag's value from the local data source.
     * @param refresh allows to trigger a background fetch of the value against the remote data source.
     */
    public fun observe(userId: UserId, featureId: FeatureId, refresh: Boolean = false): Flow<FeatureFlag?>

    /**
     * Get a feature flag's value from the local data source.
     * @param refresh allows to trigger a background fetch of the value against the remote data source.
     */
    public suspend fun get(userId: UserId, featureId: FeatureId, refresh: Boolean = false): FeatureFlag?

    /**
     * Fetches the given featureIds from the remote data source and stores them in the local one.
     * @param featureIds a list of features to be fetched. Passing any id that does not exist in the
     * remote data source will not have no consequence (said ids will just be ignored).
     */
    public suspend fun prefetch(userId: UserId, featureIds: List<FeatureId>)
}
