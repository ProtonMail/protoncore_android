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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.entity.TelemetryPriority

public fun LifecycleOwner.setupViewMetrics(
    block: () -> Unit
) {
    val observer = object : DefaultLifecycleObserver {
        // The callback is registered during STARTED state, because we want to register it as the last one.
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            owner.lifecycle.removeObserver(this)

            block()
        }
    }
    lifecycle.addObserver(observer)
}

public fun measureOnViewClicked(
    event: String,
    delegateOwner: ProductMetricsDelegateOwner,
    productDimensions: Map<String, String> = emptyMap(),
    priority: TelemetryPriority = TelemetryPriority.Default
) {
    val delegate = delegateOwner.productMetricsDelegate
    val telemetryEvent = TelemetryEvent(
        group = delegate.productGroup,
        name = event,
        dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + productDimensions,
    )

    delegate.telemetryManager.enqueue(
        userId = delegate.userId,
        event = telemetryEvent,
        priority = priority
    )
}

public fun measureOnViewFocused(
    event: String,
    delegateOwner: ProductMetricsDelegateOwner,
    productDimensions: Map<String, String> = emptyMap(),
    priority: TelemetryPriority = TelemetryPriority.Default
) {
    val delegate = delegateOwner.productMetricsDelegate
    val telemetryEvent = TelemetryEvent(
        group = delegate.productGroup,
        name = event,
        dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + productDimensions,
    )

    delegate.telemetryManager.enqueue(
        userId = delegate.userId,
        event = telemetryEvent,
        priority = priority
    )
}
