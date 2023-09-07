/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.telemetry.domain.entity.TelemetryEvent

@Serializable
internal data class DataStatsMultipleRequest constructor(
    @SerialName("EventInfo") val eventInfo: List<StatsEvent>
)

@Serializable
internal data class StatsEvent constructor(
    @SerialName("MeasurementGroup") val measurementGroup: String,
    @SerialName("Event") val event: String,
    @SerialName("Values") val values: Map<String, Float>,
    @SerialName("Dimensions") val dimensions: Map<String, String>
) {
    companion object {
        fun fromTelemetryEvent(event: TelemetryEvent): StatsEvent = StatsEvent(
            measurementGroup = event.group,
            event = event.name,
            values = event.values,
            dimensions = event.dimensions
        )
    }
}
