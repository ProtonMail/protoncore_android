/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.featureflag.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.LogTag
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

public class FeatureFlagRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
) : FeatureFlagRemoteDataSource {

    override suspend fun get(userId: UserId?, ids: List<FeatureId>): List<FeatureFlag> =
        apiProvider.get<FeaturesApi>(userId).invoke {
            getFeatureFlags(
                code = ids.joinToString(separator = ",") { it.id }
            ).features.map { it.toFeatureFlag(userId) }.also { list ->
                val mapById = list.associateBy { it.featureId }
                ids.forEach { currentId ->
                    if (!mapById.contains(currentId)) {
                        CoreLogger.i(LogTag.FEATURE_FLAG_NOT_FOUND, "FeatureFlag `$currentId` not found.")
                    }
                }
            }
        }.valueOrThrow
}
