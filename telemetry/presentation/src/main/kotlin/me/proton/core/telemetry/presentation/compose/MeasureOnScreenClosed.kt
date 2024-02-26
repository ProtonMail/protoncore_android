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

package me.proton.core.telemetry.presentation.compose

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.measureOnScreenClosed

@Composable
public fun MeasureOnScreenClosed(
    event: String,
    dimensions: Map<String, String> = emptyMap(),
    priority: TelemetryPriority = TelemetryPriority.Default
) {
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current ?: return
    val screenMetricsDelegateOwner = LocalProductMetricsDelegateOwner.current ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        onBackPressedDispatcherOwner,
        screenMetricsDelegateOwner
    ) {
        val delegate = requireNotNull(screenMetricsDelegateOwner.productMetricsDelegate) {
            "ProductMetricsDelegate is not defined."
        }
        val screenClosedDispose = measureOnScreenClosed(
            event = event,
            dimensions = dimensions,
            delegate = delegate,
            lifecycleOwner = lifecycleOwner,
            onBackPressedDispatcherOwner = onBackPressedDispatcherOwner,
            priority = priority
        )
        onDispose { screenClosedDispose() }
    }
}
