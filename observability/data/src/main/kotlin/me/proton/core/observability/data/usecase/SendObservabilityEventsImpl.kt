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

package me.proton.core.observability.data.usecase

import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.doThrow
import me.proton.core.network.domain.isRetryable
import me.proton.core.observability.data.api.ObservabilityApi
import me.proton.core.observability.data.api.request.DataMetricsRequest
import me.proton.core.observability.data.api.request.MetricEvent
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.usecase.SendObservabilityEvents
import javax.inject.Inject

public class SendObservabilityEventsImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : SendObservabilityEvents {
    override suspend fun invoke(events: List<ObservabilityEvent>) {
        val metricEvents = events.map { MetricEvent.fromObservabilityEvent(it) }
        val result = apiProvider.get<ObservabilityApi>().invoke {
            postDataMetrics(DataMetricsRequest(metricEvents))
        }
        if (result is ApiResult.Error && result.isRetryable()) {
            result.doThrow()
        } else {
            // Ignore the exception (it's already logged upstream).
            // The request cannot be retried and the events are treated as delivered.
        }
    }
}
