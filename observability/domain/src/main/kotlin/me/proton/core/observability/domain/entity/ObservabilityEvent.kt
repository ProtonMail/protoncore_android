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
import java.time.Instant

@Serializable
public data class ObservabilityEvent<D : ObservabilityData> internal constructor(
    @SerialName("Name") val name: String,
    @SerialName("Version") val version: Long,
    @SerialName("Timestamp") val timestamp: Long,
    @SerialName("Data") val data: D
) {
    public constructor(data: D, timestamp: Instant = Instant.now()) : this(
        name = data.metricName,
        version = data.metricVersion,
        timestamp = timestamp.epochSecond,
        data = data
    )
}
