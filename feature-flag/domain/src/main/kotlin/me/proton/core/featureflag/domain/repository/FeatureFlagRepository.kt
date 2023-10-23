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
     * Get a feature flag value, synchronously.
     *
     * @return [Boolean] or `null` if it is unknown.
     */
    public fun getValue(userId: UserId? = null, featureId: FeatureId): Boolean?

    /**
     * Fetches all feature flags from the remote source, and update local.
     */
    public suspend fun getAll(userId: UserId? = null): List<FeatureFlag>

    /**
     * Enqueue a refresh of all feature flags from the remote source, update local, in background.
     *
     * Note: if already enqueued, cancel previous and enqueue a new one.
     */
    public fun refreshAllOneTime(userId: UserId? = null)

    /**
     * Enqueue a refresh of all feature flags from the remote source, update local, in background.
     *
     * Note: if already enqueued do nothing, except if [immediately] is true enqueue a new one.
     */
    public fun refreshAllPeriodic(userId: UserId? = null, immediately: Boolean = false)

    /**
     * Observe a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or `null` if it is unknown remotely.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public fun observe(userId: UserId?, featureId: FeatureId, refresh: Boolean = false): Flow<FeatureFlag?>

    /**
     * Observe feature flags value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public fun observe(userId: UserId?, featureIds: Set<FeatureId>, refresh: Boolean = false): Flow<List<FeatureFlag>>

    /**
     * Get a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or `null` if it is unknown remotely.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public suspend fun get(userId: UserId?, featureId: FeatureId, refresh: Boolean = false): FeatureFlag?

    /**
     * Get feature flags value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    public suspend fun get(userId: UserId?, featureIds: Set<FeatureId>, refresh: Boolean = false): List<FeatureFlag>

    /**
     * Fetches the given featureIds from the remote source and stores them in the local one, in background.
     *
     * @param featureIds a set of features to be fetched. Passing any id that does not exist in the
     * remote source will not have no consequence (said ids will just be ignored).
     */
    public fun prefetch(userId: UserId?, featureIds: Set<FeatureId>)

    /**
     * Updates the given feature flag locally and remotely
     */
    public suspend fun update(featureFlag: FeatureFlag)
}
