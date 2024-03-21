/*
 * Copyright (c) 2024 Proton Technologies AG
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
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Feature Flag GetAll (network).")
@SchemaId("https://proton.me/android_core_featureflag_getall_total_v1.schema.json")
@Deprecated("Please do not use. Kept for documentation.")
private data class FeatureFlagGetAllTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(
        status: ApiStatus
    ) : this(LabelsData(status))

    public constructor(
        throwable: Throwable
    ): this(throwable.toHttpApiStatus().toFeatureFlagApiStatus())

    @Serializable
    public data class LabelsData constructor(
        val status: ApiStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
        http1xx,
        http2xx,
        http3xx,
        http409,
        http422,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        cancellation,
        unknown
    }
}

private fun HttpApiStatus.toFeatureFlagApiStatus(): FeatureFlagGetAllTotal.ApiStatus = when (this) {
    HttpApiStatus.http1xx -> FeatureFlagGetAllTotal.ApiStatus.http1xx
    HttpApiStatus.http2xx -> FeatureFlagGetAllTotal.ApiStatus.http2xx
    HttpApiStatus.http3xx -> FeatureFlagGetAllTotal.ApiStatus.http3xx
    HttpApiStatus.http4xx -> FeatureFlagGetAllTotal.ApiStatus.http4xx
    HttpApiStatus.http5xx -> FeatureFlagGetAllTotal.ApiStatus.http5xx
    HttpApiStatus.connectionError -> FeatureFlagGetAllTotal.ApiStatus.connectionError
    HttpApiStatus.notConnected -> FeatureFlagGetAllTotal.ApiStatus.notConnected
    HttpApiStatus.parseError -> FeatureFlagGetAllTotal.ApiStatus.parseError
    HttpApiStatus.sslError -> FeatureFlagGetAllTotal.ApiStatus.sslError
    HttpApiStatus.cancellation -> FeatureFlagGetAllTotal.ApiStatus.cancellation
    HttpApiStatus.unknown -> FeatureFlagGetAllTotal.ApiStatus.unknown
}
