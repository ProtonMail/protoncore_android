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

package me.proton.core.telemetry.presentation

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent

public interface ProductMetricsDelegate {
    public val telemetryManager: TelemetryManager

    public val productGroup: String
    public val productFlow: String
    public val productDimensions: Map<String, String> get() = emptyMap()
    public val userId: UserId? get() = null

    public fun Result<*>.toTelemetryEvent(
        name: String,
        dimensions: Map<String, String> = emptyMap()
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(KEY_FLOW to productFlow)
            .plus(KEY_RESULT to if (isSuccess) VALUE_SUCCESS else VALUE_FAILURE)
            .plus(dimensions),
        values = mutableMapOf<String, Float>().apply {
            putHttpCodeIfNotNull(this@toTelemetryEvent)
        }
    )

    private fun MutableMap<String, Float>.putHttpCodeIfNotNull(result: Result<*>) {
        result.getHttpCode()?.let { put(KEY_HTTP_CODE, it.toFloat()) }
    }

    private fun Result<*>.getHttpCode(): Int? {
        return ((exceptionOrNull() as? ApiException)?.error as? ApiResult.Error.Http)?.httpCode
    }

    public companion object {
        public const val KEY_FLOW: String = "flow"
        public const val KEY_RESULT: String = "result"
        public const val KEY_HTTP_CODE: String = "http_code"

        public const val VALUE_SUCCESS: String = "success"
        public const val VALUE_FAILURE: String = "failure"
    }
}
