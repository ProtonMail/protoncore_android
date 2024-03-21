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
import me.proton.core.featureflag.domain.entity.Scope

/**
 * Manager to access [FeatureFlag]s.
 *
 * The suggested usage is for clients to call [prefetch] at boot.
 *
 * Note: Clients should take care of implementing the logic to fallback to default value.
 */
public interface FeatureFlagManager {

    /**
     * Awaits until a non-empty Scope is available locally.
     *
     * Note: May suspend indefinitely, if a given scope is never fetched or is empty.
     */
    @ExperimentalProtonFeatureFlag
    @Deprecated("Will be removed when CredentialLessDisabled FF will be removed.")
    public suspend fun awaitNotEmptyScope(
        userId: UserId? = null,
        scope: Scope
    )

    /**
     * Get a feature flag value.
     *
     * @return value, otherwise false.
     */
    @ExperimentalProtonFeatureFlag
    public fun getValue(
        userId: UserId? = null,
        featureId: FeatureId
    ): Boolean

    /**
     * Get a fresh feature flag value (refresh all before returning).
     *
     * @return [getValue], on success or any error.
     */
    @ExperimentalProtonFeatureFlag
    public suspend fun getFreshValue(
        userId: UserId? = null,
        featureId: FeatureId
    ): Boolean

    /**
     * Enqueue a refresh of all feature flags from the remote source and stores them, in background.
     *
     * Note: if already enqueued, cancel previous and enqueue a new one.
     */
    @ExperimentalProtonFeatureFlag
    public fun refreshAll(
        userId: UserId? = null
    )

    /**
     * Observe a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or `null` if it is unknown remotely.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    @Deprecated("A new synchronous API will replace this.")
    public fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean = false
    ): Flow<FeatureFlag?>

    /**
     * Observe a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or [default] if it is unknown remotely or on error.
     */
    @Deprecated("A new synchronous API will replace this.")
    public fun observeOrDefault(
        userId: UserId?,
        featureId: FeatureId,
        default: FeatureFlag,
        refresh: Boolean = false
    ): Flow<FeatureFlag>

    /**
     * Get a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or `null` if it is unknown remotely.
     *
     * @throws me.proton.core.network.domain.ApiException on remote source error
     */
    @Deprecated("A new synchronous API will replace this.")
    public suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean = false
    ): FeatureFlag?

    /**
     * Get a feature flag value from the local source, if exist, or from remote source otherwise.
     *
     * @param refresh allows to force refresh against the remote source.
     *
     * @return [FeatureFlag] or [default] if it is unknown remotely or on error.
     */
    @Deprecated("A new synchronous API will replace this.")
    public suspend fun getOrDefault(
        userId: UserId?,
        featureId: FeatureId,
        default: FeatureFlag,
        refresh: Boolean = false
    ): FeatureFlag

    /**
     * Fetches the given featureIds from the remote source and stores them in the local one, in background.
     *
     * @param featureIds a set of features to be fetched. Passing any id that does not exist in the
     * remote source will not have no consequence (said ids will just be ignored).
     */
    @Deprecated("A new synchronous API will replace this.")
    public fun prefetch(
        userId: UserId?,
        featureIds: Set<FeatureId>
    )

    /**
     * Updates the given feature flag with the given values
     */
    @Deprecated("A new synchronous API will replace this.")
    public suspend fun update(
        featureFlag: FeatureFlag
    )
}

@RequiresOptIn(message = "This API is experimental. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class ExperimentalProtonFeatureFlag
