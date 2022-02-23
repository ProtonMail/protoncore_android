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
import me.proton.core.data.arch.ProtonStore
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.api.FeaturesApi
import me.proton.core.featureflag.data.api.response.FeatureApiResponse
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FeatureFlagRepositoryImpl @Inject internal constructor(
    database: FeatureFlagDatabase,
    private val apiProvider: ApiProvider
) : FeatureFlagRepository {

    private val featureFlagDao = database.featureFlagDao()

    private data class StoreKey(val userId: UserId?, val featureId: FeatureId)

    private val store: ProtonStore<StoreKey, FeatureFlag> = StoreBuilder.from(
        fetcher = Fetcher.of { key: StoreKey ->
            apiProvider.get<FeaturesApi>(key.userId).invoke {
                getFeatureFlags(key.featureId.id).features.first()
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: StoreKey ->
                featureFlagDao.observe(key.userId, key.featureId.id).map { it?.toFeatureFlag() }
            },
            writer = { key, apiResponse: FeatureApiResponse ->
                featureFlagDao.insertOrUpdate(apiResponse.toEntity(key.userId))
            },
        )
    ).buildProtonStore()

    override fun observe(userId: UserId?, featureId: FeatureId, refresh: Boolean): Flow<FeatureFlag?> =
        StoreKey(userId = userId, featureId = featureId).let { key ->
            store.stream(StoreRequest.cached(key, refresh)).map { it.dataOrNull() }
        }

    override suspend fun get(userId: UserId?, featureId: FeatureId, refresh: Boolean): FeatureFlag =
        StoreKey(userId = userId, featureId = featureId).let { key ->
            if (refresh) store.fresh(key) else store.get(key)
        }

    override suspend fun prefetch(userId: UserId?, featureIds: List<FeatureId>) {
        val apiResponse = apiProvider.get<FeaturesApi>(userId).invoke {
            getFeatureFlags(featureIds.joinToString(separator = ",") { it.id })
        }.valueOrNull

        apiResponse?.let {
            val entities = apiResponse.features.map { it.toEntity(userId) }.toTypedArray()
            featureFlagDao.insertOrUpdate(*entities)
        }
    }
}
