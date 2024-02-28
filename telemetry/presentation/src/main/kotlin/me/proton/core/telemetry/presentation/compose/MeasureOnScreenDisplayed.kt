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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.measureOnScreenDisplayed

@Composable
public fun MeasureOnScreenDisplayed(
    event: String,
    dimensions: Map<String, String> = emptyMap(),
    priority: TelemetryPriority = TelemetryPriority.Default
) {
    val screenMetricsDelegateOwner = LocalProductMetricsDelegateOwner.current ?: return
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

    DisposableEffect(
        lifecycleOwner,
        savedStateRegistryOwner,
        screenMetricsDelegateOwner
    ) {
        val screenDisplayedDispose = measureOnScreenDisplayed(
            event = event,
            dimensions = dimensions,
            delegateOwner = screenMetricsDelegateOwner,
            lifecycleOwner = lifecycleOwner,
            savedStateRegistryOwner = savedStateRegistryOwner,
            priority = priority
        )
        onDispose { screenDisplayedDispose() }
    }
}
