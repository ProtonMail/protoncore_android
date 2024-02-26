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

package me.proton.core.telemetry.presentation.compose

import androidx.compose.runtime.Composable
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.measureOnViewFocused

@Composable
public fun MeasureOnViewFocused(
    event: String,
    dimensions: Map<String, String> = emptyMap(),
    priority: TelemetryPriority = TelemetryPriority.Default
) {
    val screenMetricsDelegateOwner = LocalProductMetricsDelegateOwner.current ?: return
    val delegate = requireNotNull(screenMetricsDelegateOwner.productMetricsDelegate) {
        "ProductMetricsDelegate is not defined."
    }
    measureOnViewFocused(
        event = event,
        delegate = delegate,
        dimensions = dimensions,
        priority = priority
    )
}

@Composable
public fun MeasureOnViewFocused(
    event: String,
    item: String,
    priority: TelemetryPriority = TelemetryPriority.Default
): Unit = MeasureOnViewFocused(
    event = event,
    dimensions = mapOf(ProductMetricsDelegate.KEY_ITEM to item),
    priority = priority
)
