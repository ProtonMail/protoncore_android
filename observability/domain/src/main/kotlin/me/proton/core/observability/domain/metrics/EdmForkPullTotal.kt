/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.isHttpError
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Pulling a session fork for Easy Device Migration.")
@SchemaId("https://proton.me/android_core_edm_fork_pull_total_v1.schema.json")
public class EdmForkPullTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(result: Result<*>) : this(LabelsData(result.toStatus()))

    @Serializable
    public data class LabelsData(
        val status: ApiStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
        http1xx,
        http2xx,
        http3xx,
        http4xx,
        http5xx,
        connectionError,
        forkPending,
        notConnected,
        parseError,
        sslError,
        cancellation,
        unknown
    }

    internal companion object {
        fun <R> Result<R>.toStatus(): ApiStatus = when {
            isHttpError(HTTP_UNPROCESSABLE) -> ApiStatus.forkPending
            else -> toHttpApiStatus().toApiStatus()
        }

        internal fun HttpApiStatus.toApiStatus(): ApiStatus = when (this) {
            HttpApiStatus.http1xx -> ApiStatus.http1xx
            HttpApiStatus.http2xx -> ApiStatus.http2xx
            HttpApiStatus.http3xx -> ApiStatus.http3xx
            HttpApiStatus.http4xx -> ApiStatus.http4xx
            HttpApiStatus.http5xx -> ApiStatus.http5xx
            HttpApiStatus.connectionError -> ApiStatus.connectionError
            HttpApiStatus.notConnected -> ApiStatus.notConnected
            HttpApiStatus.parseError -> ApiStatus.parseError
            HttpApiStatus.sslError -> ApiStatus.sslError
            HttpApiStatus.cancellation -> ApiStatus.cancellation
            HttpApiStatus.unknown -> ApiStatus.unknown
        }
    }
}
