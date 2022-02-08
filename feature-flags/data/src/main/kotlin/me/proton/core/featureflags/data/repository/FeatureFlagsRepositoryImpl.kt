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

package me.proton.core.featureflags.data.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflags.data.api.FeaturesApi
import me.proton.core.featureflags.data.db.FeatureFlagDatabase
import me.proton.core.featureflags.domain.entity.FeatureFlag
import me.proton.core.featureflags.domain.entity.FeatureId
import me.proton.core.featureflags.domain.repository.FeatureFlagsRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult

class FeatureFlagsRepositoryImpl(
    database: FeatureFlagDatabase,
    private val apiProvider: ApiProvider
) : FeatureFlagsRepository {

    private val featureFlagDao = database.featureFlagDao()

    override fun observe(userId: UserId, feature: FeatureId): Flow<DataResult<FeatureFlag>> {
        TODO("Not yet implemented")
    }

    override suspend fun get(userId: UserId, feature: FeatureId): DataResult<FeatureFlag> {
        val localFlagEntity = featureFlagDao.get(userId, feature.id)
        localFlagEntity?.let {
            return DataResult.Success(ResponseSource.Local, it.toFeatureFlag())
        }

        val apiResult = apiProvider.get<FeaturesApi>(userId).invoke { getFeatureFlag(feature.id) }
        return when (apiResult) {
            is ApiResult.Success -> DataResult.Success(
                ResponseSource.Remote,
                apiResult.value.toFeatureFlags()
            )
            is ApiResult.Error -> DataResult.Error.Remote(
                "Failed fetching Feature Flag value for ${feature.id}",
                apiResult.cause
            )
        }

    }

}
