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

package me.proton.core.telemetry.presentation

import me.proton.core.telemetry.domain.entity.TelemetryEvent

public fun measureOnViewClicked(
    event: String,
    delegateOwner: ProductMetricsDelegateOwner,
    productDimensions: Map<String, String> = emptyMap(),
) {
    val delegate = delegateOwner.productMetricsDelegate
    val telemetryEvent = TelemetryEvent(
        group = delegate.productGroup,
        name = event,
        dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + productDimensions,
    )

    delegate.telemetryManager.enqueue(
        userId = delegate.userId,
        event = telemetryEvent
    )
}


public fun measureOnViewFocused(
    event: String,
    delegateOwner: ProductMetricsDelegateOwner,
    productDimensions: Map<String, String> = emptyMap()
) {
    val delegate = delegateOwner.productMetricsDelegate
    val telemetryEvent = TelemetryEvent(
        group = delegate.productGroup,
        name = event,
        dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + productDimensions,
    )

    delegate.telemetryManager.enqueue(
        userId = delegate.userId,
        event = telemetryEvent
    )
}
