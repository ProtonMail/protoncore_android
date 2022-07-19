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

package me.proton.core.featureflag.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.remote.worker.FetchFeatureIdsWorker
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FeatureFlagRepositoryImpl @Inject internal constructor(
    private val localDataSource: FeatureFlagLocalDataSource,
    private val remoteDataSource: FeatureFlagRemoteDataSource,
    private val workManager: WorkManager,
) : FeatureFlagRepository {

    private data class StoreKey(val userId: UserId?, val featureIds: List<FeatureId>)

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey -> remoteDataSource.get(key.userId, key.featureIds) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: StoreKey ->
                localDataSource.observe(key.userId, key.featureIds).map { list ->
                    list.takeIf { it.isNotEmpty() }
                }
            },
            writer = { _, list: List<FeatureFlag> -> localDataSource.upsert(list) },
        )
    ).buildProtonStore()

    override fun observe(
        userId: UserId?,
        featureIds: List<FeatureId>,
        refresh: Boolean
    ): Flow<List<FeatureFlag>> = StoreKey(userId = userId, featureIds = featureIds).let { key ->
        store.stream(StoreRequest.cached(key, refresh)).map { it.dataOrNull().orEmpty() }
    }

    override suspend fun get(
        userId: UserId?,
        featureIds: List<FeatureId>,
        refresh: Boolean
    ): List<FeatureFlag> = StoreKey(userId = userId, featureIds = featureIds).let { key ->
        if (refresh) store.fresh(key) else store.get(key)
    }

    override fun observe(userId: UserId?, featureId: FeatureId, refresh: Boolean): Flow<FeatureFlag?> =
        observe(userId, listOf(featureId), refresh).map { it.firstOrNull() }

    override suspend fun get(userId: UserId?, featureId: FeatureId, refresh: Boolean): FeatureFlag? =
        get(userId, listOf(featureId), refresh).firstOrNull()

    override fun prefetch(userId: UserId?, featureIds: List<FeatureId>) {
        // Replace any existing FetchFeatureIdsWorker.
        workManager.enqueueUniqueWork(
            getUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            FetchFeatureIdsWorker.getRequest(userId, featureIds),
        )
    }

    private companion object {
        // Currently for: FetchFeatureIdsWorker (ExistingWorkPolicy.REPLACE).
        private fun getUniqueWorkName(userId: UserId?) =
            "${FeatureFlagRepositoryImpl::class.simpleName}-prefetch-$userId"
    }
}
