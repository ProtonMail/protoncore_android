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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import javax.inject.Inject
import kotlin.time.Duration

public class FeatureFlagManagerImpl @Inject internal constructor(
    private val repository: FeatureFlagRepository
) : FeatureFlagManager {

    @ExperimentalProtonFeatureFlag
    public override suspend fun awaitNotEmptyScope(
        userId: UserId?,
        scope: Scope
    ): Unit = repository.awaitNotEmptyScope(userId, scope)

    @ExperimentalProtonFeatureFlag
    override fun getValue(
        userId: UserId?,
        featureId: FeatureId
    ): Boolean = repository.getValue(userId, featureId) ?: false

    @ExperimentalProtonFeatureFlag
    override suspend fun getFreshValue(
        userId: UserId?,
        featureId: FeatureId
    ): Boolean = runCatching { repository.getAll(userId) }.run { getValue(userId, featureId) }

    @ExperimentalProtonFeatureFlag
    override fun refreshAll(
        userId: UserId?
    ): Unit = repository.refreshAllOneTime(userId)

    override fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): Flow<FeatureFlag?> = repository.observe(userId, featureId, refresh).distinctUntilChanged()

    override fun observeOrDefault(
        userId: UserId?,
        featureId: FeatureId,
        default: FeatureFlag,
        refresh: Boolean
    ): Flow<FeatureFlag> = observe(userId, featureId, refresh).map { it ?: default }

    override suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): FeatureFlag? = repository.get(userId, featureId, refresh)

    override suspend fun getOrDefault(
        userId: UserId?,
        featureId: FeatureId,
        default: FeatureFlag,
        refresh: Boolean
    ): FeatureFlag = runCatching { get(userId, featureId, refresh) }.getOrNull() ?: default

    override fun prefetch(
        userId: UserId?,
        featureIds: Set<FeatureId>
    ): Unit = repository.prefetch(userId, featureIds)

    override suspend fun update(featureFlag: FeatureFlag): Unit = repository.update(featureFlag)

}
