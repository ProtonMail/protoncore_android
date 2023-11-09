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

package me.proton.core.humanverification.presentation.telemetry

import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.KEY_ITEM
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.KEY_RESULT

internal interface ProductMetricsDelegateHv : ProductMetricsDelegate {
    fun toTelemetryEvent(
        name: String,
        item: String
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(KEY_ITEM to item)
    )

    fun toTelemetryEvent(
        name: String,
        isSuccess: Boolean
    ): TelemetryEvent =
        TelemetryEvent(
            name = name,
            group = productGroup,
            dimensions = productDimensions
                .plus(KEY_RESULT to isSuccess.toDimensionValue()),
        )

    private fun Boolean.toDimensionValue(): String = when (this) {
        true -> ProductMetricsDelegate.VALUE_SUCCESS
        false -> ProductMetricsDelegate.VALUE_FAILURE
    }
}
