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

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.remote.worker.FeatureFlagWorkerManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FeatureFlagRepositoryImpl @Inject internal constructor(
    private val localDataSource: FeatureFlagLocalDataSource,
    private val remoteDataSource: FeatureFlagRemoteDataSource,
    private val workerManager: FeatureFlagWorkerManager,
    scopeProvider: CoroutineScopeProvider
) : FeatureFlagRepository {

    private data class StoreKey(val userId: UserId?, val featureIds: Set<FeatureId>)

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey -> remoteDataSource.get(key.userId, key.featureIds) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: StoreKey ->
                localDataSource.observe(key.userId, key.featureIds).map { list ->
                    val localFlags = list.associateBy { it.featureId }
                    list.takeIf { key.featureIds.all { it in localFlags } }
                }
            },
            writer = { key: StoreKey, fetched: List<FeatureFlag> ->
                val unknownIds = key.featureIds - fetched.map { it.featureId }.toSet()
                val unknownFlags = unknownIds.map { FeatureFlag.default(it.id, false) }
                localDataSource.upsert(fetched + unknownFlags)
            },
        )
    ).disableCache().buildProtonStore(scopeProvider)

    private val unleashFeatureMapMutex = Mutex()
    private var unleashFeatureMap = mutableMapOf<UserId?, MutableMap<FeatureId, FeatureFlag>>()

    init { scopeProvider.GlobalIOSupervisedScope.launch { putAllUnleashInMemory() } }

    private suspend fun putAllUnleashInMemory() = unleashFeatureMapMutex.withLock {
        val list = localDataSource.getAll(Scope.Unleash)
        mutableMapOf<UserId?, MutableMap<FeatureId, FeatureFlag>>().let { map ->
            list.forEach { map.getOrPut(it.userId) { mutableMapOf() }[it.featureId] = it }
            unleashFeatureMap = map
        }
    }

    override fun getValue(
        userId: UserId?,
        featureId: FeatureId
    ): Boolean? = unleashFeatureMap[userId]?.get(featureId)?.value

    override suspend fun getAll(
        userId: UserId?
    ): List<FeatureFlag> = remoteDataSource.getAll(userId).also { list ->
        localDataSource.replaceAll(userId, Scope.Unleash, list)
        putAllUnleashInMemory()
    }

    override fun refreshAllOneTime(
        userId: UserId?
    ): Unit = workerManager.enqueueOneTime(userId)

    override fun refreshAllPeriodic(
        userId: UserId?,
        immediately: Boolean
    ): Unit = workerManager.enqueuePeriodic(userId, immediately = immediately)

    override fun observe(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): Flow<List<FeatureFlag>> = StoreKey(userId = userId, featureIds = featureIds).let { key ->
        store.stream(StoreRequest.cached(key, refresh))
            .map { it.dataOrNull().orEmpty().filterNot { flag -> flag.scope == Scope.Unknown } }
    }

    override suspend fun get(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): List<FeatureFlag> = StoreKey(userId = userId, featureIds = featureIds).let { key ->
        if (refresh) store.fresh(key) else store.get(key)
    }.filterNot { flag -> flag.scope == Scope.Unknown }

    override fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): Flow<FeatureFlag?> =
        observe(userId, setOf(featureId), refresh).map { it.firstOrNull() }

    override suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): FeatureFlag? =
        get(userId, setOf(featureId), refresh).firstOrNull()

    override fun prefetch(userId: UserId?, featureIds: Set<FeatureId>) {
        remoteDataSource.prefetch(userId, featureIds)
    }

    override suspend fun update(featureFlag: FeatureFlag) {
        localDataSource.upsert(listOf(featureFlag))
        remoteDataSource.update(featureFlag)
    }
}
