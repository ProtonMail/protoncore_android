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

package me.proton.core.featureflag.domain

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId

/**
 * Manager to access Remote [FeatureFlag]s.
 *
 * The suggested usage is for clients to call [prefetch] at boot, passing a list of all the featureIds
 * that will be needed during the lifecycle. This will help ensuring that the flags' values are always
 * up-to-date with remote while avoiding to perform an API call to each time we need a flag.
 *
 * Clients should take care of implementing some logic to use default values when it wasn't possible to
 * receive them through this repo.
 */
public interface FeatureFlagManager {

    /**
     * Observe a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force a background fetch of the value against the remote source.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public fun observe(userId: UserId?, featureId: FeatureId, refresh: Boolean = false): Flow<FeatureFlag?>

    /**
     * Get a feature flag's value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force a background fetch of the value against the remote source.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public suspend fun get(userId: UserId?, featureId: FeatureId, refresh: Boolean = false): FeatureFlag?

    /**
     * Fetches the given featureIds from the remote source and stores them in the local one, in background.
     *
     * @param featureIds a list of features to be fetched. Passing any id that does not exist in the
     * remote source will not have no consequence (said ids will just be ignored).
     */
    public fun prefetch(userId: UserId?, featureIds: List<FeatureId>)
}
