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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.api.FeaturesApi
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiProvider

class FeatureFlagRepositoryImpl(
    database: FeatureFlagDatabase,
    private val apiProvider: ApiProvider
) : FeatureFlagRepository {

    private val featureFlagDao = database.featureFlagDao()

    override fun observe(userId: UserId, feature: FeatureId): Flow<FeatureFlag?> {
        return featureFlagDao.observe(userId, feature.id).mapLatest { dbFlag ->
            if (dbFlag == null) {
                return@mapLatest fetchFromApi(userId, feature)?.toEntity(userId)?.let {
                    featureFlagDao.insertOrUpdate(it)
                    return@let it.toFeatureFlag()
                }
            }

            dbFlag.toFeatureFlag()
        }
    }

    override suspend fun get(userId: UserId, feature: FeatureId): FeatureFlag? {
        val localFlagEntity = featureFlagDao.get(userId, feature.id)
        localFlagEntity?.let {
            return it.toFeatureFlag()
        }

        return fetchFromApi(userId, feature)?.toEntity(userId)?.let {
            featureFlagDao.insertOrUpdate(it)
            it.toFeatureFlag()
        }
    }

    private suspend fun fetchFromApi(
        userId: UserId,
        feature: FeatureId
    ) = apiProvider.get<FeaturesApi>(userId).invoke {
        getFeatureFlag(feature.id).features.first()
    }.valueOrNull

}
