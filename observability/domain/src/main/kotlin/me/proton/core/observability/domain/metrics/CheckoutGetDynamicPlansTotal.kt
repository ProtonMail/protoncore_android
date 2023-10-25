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
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.isHttpError
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Querying for a current dynamic subscription.")
@SchemaId("https://proton.me/android_core_checkout_dynamicPlans_getDynamicPlans_total_v1.schema.json")
public data class CheckoutGetDynamicPlansTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: ApiStatus) : this(LabelsData(status))

    public constructor(result: Result<*>) : this(result.toGetDynamicPlansApiStatus())

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

internal fun Result<*>.toGetDynamicPlansApiStatus(): CheckoutGetDynamicPlansTotal.ApiStatus = when {
    isHttpError(HttpResponseCodes.HTTP_CONFLICT) -> CheckoutGetDynamicPlansTotal.ApiStatus.http409
    isHttpError(HttpResponseCodes.HTTP_UNPROCESSABLE) -> CheckoutGetDynamicPlansTotal.ApiStatus.http422
    else -> toHttpApiStatus().toGetDynamicPlansApiStatus()
}

public fun HttpApiStatus.toGetDynamicPlansApiStatus(): CheckoutGetDynamicPlansTotal.ApiStatus =
    when (this) {
        HttpApiStatus.http1xx -> CheckoutGetDynamicPlansTotal.ApiStatus.http1xx
        HttpApiStatus.http2xx -> CheckoutGetDynamicPlansTotal.ApiStatus.http2xx
        HttpApiStatus.http3xx -> CheckoutGetDynamicPlansTotal.ApiStatus.http3xx
        HttpApiStatus.http4xx -> CheckoutGetDynamicPlansTotal.ApiStatus.http4xx
        HttpApiStatus.http5xx -> CheckoutGetDynamicPlansTotal.ApiStatus.http5xx
        HttpApiStatus.connectionError -> CheckoutGetDynamicPlansTotal.ApiStatus.connectionError
        HttpApiStatus.notConnected -> CheckoutGetDynamicPlansTotal.ApiStatus.notConnected
        HttpApiStatus.parseError -> CheckoutGetDynamicPlansTotal.ApiStatus.parseError
        HttpApiStatus.sslError -> CheckoutGetDynamicPlansTotal.ApiStatus.sslError
        HttpApiStatus.cancellation -> CheckoutGetDynamicPlansTotal.ApiStatus.cancellation
        HttpApiStatus.unknown -> CheckoutGetDynamicPlansTotal.ApiStatus.unknown
    }
