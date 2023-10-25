/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.hasProtonErrorCode
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Cancel an account recovery attempt.")
@SchemaId("https://proton.me/android_core_accountRecovery_cancellation_total_v1.schema.json")
public data class AccountRecoveryCancellationTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : ObservabilityData() {
    public constructor(result: Result<*>) : this(result.toApiStatus())

    public constructor(status: ApiStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData constructor(
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
        notConnected,
        parseError,
        sslError,
        wrongPassword,
        cancellation,
        unknown
    }
}

private fun <R> Result<R>.toApiStatus(): AccountRecoveryCancellationTotal.ApiStatus = when {
    exceptionOrNull()?.hasProtonErrorCode(ResponseCodes.PASSWORD_WRONG) == true ->
        AccountRecoveryCancellationTotal.ApiStatus.wrongPassword

    else -> toHttpApiStatus().toApiStatus()
}

private fun HttpApiStatus.toApiStatus(): AccountRecoveryCancellationTotal.ApiStatus = when (this) {
    HttpApiStatus.http1xx -> AccountRecoveryCancellationTotal.ApiStatus.http1xx
    HttpApiStatus.http2xx -> AccountRecoveryCancellationTotal.ApiStatus.http2xx
    HttpApiStatus.http3xx -> AccountRecoveryCancellationTotal.ApiStatus.http3xx
    HttpApiStatus.http4xx -> AccountRecoveryCancellationTotal.ApiStatus.http4xx
    HttpApiStatus.http5xx -> AccountRecoveryCancellationTotal.ApiStatus.http5xx
    HttpApiStatus.connectionError -> AccountRecoveryCancellationTotal.ApiStatus.connectionError
    HttpApiStatus.notConnected -> AccountRecoveryCancellationTotal.ApiStatus.notConnected
    HttpApiStatus.parseError -> AccountRecoveryCancellationTotal.ApiStatus.parseError
    HttpApiStatus.sslError -> AccountRecoveryCancellationTotal.ApiStatus.sslError
    HttpApiStatus.cancellation -> AccountRecoveryCancellationTotal.ApiStatus.cancellation
    HttpApiStatus.unknown -> AccountRecoveryCancellationTotal.ApiStatus.unknown
}
