/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.metrics.data.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.data.remote.MetricsApi
import me.proton.core.metrics.data.remote.request.toMetricsRequest
import me.proton.core.metrics.domain.entity.Metrics
import me.proton.core.metrics.domain.repository.MetricsRepository
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class MetricsRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : MetricsRepository {

    override suspend fun post(userId: UserId?, metrics: Metrics) {
        apiProvider.get<MetricsApi>(userId).invoke {
            postMetrics(metrics.toMetricsRequest())
        }.throwIfError()
    }
}
