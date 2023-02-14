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

package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Loading the Verify web app inside Android's web view.")
@SchemaId("https://proton.me/android_core_hv_pageLoad_total_v1.schema.json")
public data class HvPageLoadTotalV1(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: Status, routing: Routing) : this(LabelsData(status, routing))

    @Serializable
    public data class LabelsData constructor(
        val status: Status,
        val routing: Routing
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class Status {
        http2xx,
        http4xx,
        http5xx,
        connectionError,
        sslError
    }

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class Routing {
        standard,
        alternative
    }
}
