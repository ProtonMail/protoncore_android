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
@SchemaId("https://proton.me/android_core_checkout_dynamicPlans_getDynamicSubscription_total_v1.schema.json")
public data class CheckoutGetDynamicSubscriptionTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: ApiStatus) : this(LabelsData(status))

    public constructor(result: Result<*>) : this(result.toGetDynamicSubscriptionApiStatus())

    @Serializable
    public data class LabelsData constructor(
        val status: ApiStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
        http2xx,
        failureNoSubscription,
        http409,
        http422,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        unknown
    }
}

internal fun Result<*>.toGetDynamicSubscriptionApiStatus(): CheckoutGetDynamicSubscriptionTotal.ApiStatus = when {
    isHttpError(HttpResponseCodes.HTTP_CONFLICT) -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.http409
    isHttpError(HttpResponseCodes.HTTP_UNPROCESSABLE) -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.http422
    else -> toHttpApiStatus().toGetDynamicSubscriptionApiStatus()
}

public fun HttpApiStatus.toGetDynamicSubscriptionApiStatus(): CheckoutGetDynamicSubscriptionTotal.ApiStatus =
    when (this) {
        HttpApiStatus.http2xx -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.http2xx
        HttpApiStatus.http4xx -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.http4xx
        HttpApiStatus.http5xx -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.http5xx
        HttpApiStatus.connectionError -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.connectionError
        HttpApiStatus.notConnected -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.notConnected
        HttpApiStatus.parseError -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.parseError
        HttpApiStatus.sslError -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.sslError
        HttpApiStatus.unknown -> CheckoutGetDynamicSubscriptionTotal.ApiStatus.unknown
    }
