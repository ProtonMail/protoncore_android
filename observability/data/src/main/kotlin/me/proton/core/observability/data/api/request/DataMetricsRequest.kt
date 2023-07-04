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

package me.proton.core.observability.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.util.kotlin.ProtonCoreConfig.defaultJson
import me.proton.core.util.kotlin.serializeToJsonElement

@Serializable
internal data class DataMetricsRequest constructor(
    @SerialName("Metrics") val metrics: List<MetricEvent>
)

@Serializable
internal data class MetricEvent constructor(
    @SerialName("Name") val name: String,
    @SerialName("Version") val version: Long,
    @SerialName("Timestamp") val timestamp: Long,
    @SerialName("Data") val data: JsonElement
) {
    companion object {
        fun fromObservabilityEvent(event: ObservabilityEvent): MetricEvent = MetricEvent(
            name = event.name,
            version = event.version,
            timestamp = event.timestamp,
            data = JsonObject(
                event.data.serializeToJsonElement().jsonObject.toMutableMap().apply {
                    // The server doesn't allow extra properties — remove "type" property:
                    remove(defaultJson.configuration.classDiscriminator)
                }
            )
        )
    }
}
