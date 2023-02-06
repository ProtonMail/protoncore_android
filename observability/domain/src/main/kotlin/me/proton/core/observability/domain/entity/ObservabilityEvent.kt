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

package me.proton.core.observability.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.deserialize
import java.time.Instant

/**
 * @param name Metric name.
 * @param version Metric version.
 * @param timestamp Unix time in seconds.
 * @param data Observability event data.
 */
@Serializable
public data class ObservabilityEvent internal constructor(
    @Transient val id: Long? = null,
    @SerialName("Name") val name: String,
    @SerialName("Version") val version: Long,
    @SerialName("Timestamp") val timestamp: Long,
    @SerialName("Data") val data: ObservabilityData
) {
    public constructor(
        id: Long? = null,
        name: String,
        version: Long,
        timestamp: Instant = Instant.now(),
        data: String
    ) : this(
        id = id,
        name = name,
        version = version,
        timestamp = timestamp.epochSecond,
        data = data.deserialize()
    )

    public constructor(
        id: Long? = null,
        timestamp: Instant = Instant.now(),
        data: ObservabilityData
    ) : this(
        id = id,
        name = data.metricName,
        version = data.metricVersion,
        timestamp = timestamp.epochSecond,
        data = data
    )

}
