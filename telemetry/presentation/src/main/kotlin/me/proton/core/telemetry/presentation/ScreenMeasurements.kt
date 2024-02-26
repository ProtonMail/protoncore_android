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

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import me.proton.core.presentation.utils.launchOnBackPressed
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.domain.entity.TelemetryPriority

/**
 * Sets up a listener that will be automatically called when
 * a screen (associated with the [lifecycleOwner]) is displayed.
 * The listener will send a telemetry event.
 * @return A function that can be optionally called to stop the listener and clean up.
 */
internal fun measureOnScreenDisplayed(
    event: String,
    dimensions: Map<String, String> = emptyMap(),
    delegate: ProductMetricsDelegate,
    lifecycleOwner: LifecycleOwner,
    savedStateRegistryOwner: SavedStateRegistryOwner,
    priority: TelemetryPriority = TelemetryPriority.Default
): () -> Unit = lifecycleOwner.launchOnScreenView(savedStateRegistryOwner.savedStateRegistry) {
    val telemetryEvent = TelemetryEvent(
        group = delegate.productGroup,
        name = event,
        dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + dimensions,
    )

    delegate.telemetryManager.enqueue(
        userId = delegate.userId,
        event = telemetryEvent,
        priority = priority
    )
}

/**
 * Sets up a listener that will be automatically called when a user navigates back
 * from the screen that's associated with the [lifecycleOwner].
 * The listener will send a telemetry event.
 * @return A function that can be optionally called to stop the listener and clean up.
 */
internal fun measureOnScreenClosed(
    event: String,
    dimensions: Map<String, String> = emptyMap(),
    delegate: ProductMetricsDelegate,
    lifecycleOwner: LifecycleOwner,
    onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
    priority: TelemetryPriority = TelemetryPriority.Default
): () -> Unit =
    lifecycleOwner.launchOnBackPressed(onBackPressedDispatcherOwner.onBackPressedDispatcher) {
        val telemetryEvent = TelemetryEvent(
            group = delegate.productGroup,
            name = event,
            dimensions = mapOf("flow" to delegate.productFlow) + delegate.productDimensions + dimensions,
        )

        delegate.telemetryManager.enqueue(
            userId = delegate.userId,
            event = telemetryEvent,
            priority = priority
        )
    }
