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

package me.proton.core.telemetry.presentation.usecase

import android.app.Application
import android.view.View
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import me.proton.core.presentation.utils.UiComponent
import me.proton.core.presentation.utils.launchOnUiComponentCreated
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused
import me.proton.core.telemetry.presentation.measureOnScreenClosed
import me.proton.core.telemetry.presentation.measureOnScreenDisplayed
import me.proton.core.telemetry.presentation.measureOnViewClicked
import me.proton.core.telemetry.presentation.measureOnViewFocused
import javax.inject.Inject

internal class SetupProductMetrics @Inject constructor(
    private val application: Application,
    private val telemetryManager: TelemetryManager
) {
    operator fun invoke() {
        application.launchOnUiComponentCreated(this::onUiComponentCreated)
    }

    private fun onUiComponentCreated(
        lifecycleOwner: LifecycleOwner,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        component: UiComponent
    ) {
        val delegateOwner = component.value as? ProductMetricsDelegateOwner
        val productMetrics = component.value.findAnnotation<ProductMetrics>()

        if (delegateOwner != null && productMetrics != null) {
            error("Cannot use both the ${ProductMetricsDelegateOwner::class.simpleName} and ${ProductMetrics::class.simpleName} annotation in ${component.value::class.qualifiedName}.")
        } else if (delegateOwner == null && productMetrics == null) {
            return
        }

        val resolvedDelegateOwner = when {
            delegateOwner != null -> delegateOwner
            productMetrics != null -> ProductMetricsDelegateOwner(
                AnnotationProductMetricsDelegate(
                    productMetrics,
                    telemetryManager
                )
            )

            else -> error("Fatal error: both delegateOwner and screenMetrics and null.")
        }

        component.value.findAnnotation<ScreenDisplayed>()?.let { screenDisplayed ->
            measureOnScreenDisplayed(
                productEvent = screenDisplayed.event,
                productDimensions = screenDisplayed.dimensions.toMap(),
                delegateOwner = resolvedDelegateOwner,
                lifecycleOwner = lifecycleOwner,
                savedStateRegistryOwner = savedStateRegistryOwner
            )
        }

        component.value.findAnnotation<ScreenClosed>()?.let { screenClosed ->
            measureOnScreenClosed(
                productEvent = screenClosed.event,
                productDimensions = screenClosed.dimensions.toMap(),
                delegateOwner = resolvedDelegateOwner,
                lifecycleOwner = lifecycleOwner,
                onBackPressedDispatcherOwner = onBackPressedDispatcherOwner
            )
        }

        component.value.findAnnotation<ViewClicked>()?.let { viewClicked ->

            for (viewId in viewClicked.viewIds) {
                val id = component.getIdentifier(viewId)
                val view = component.findViewById<View>(id)
                view.setOnClickListener { _ ->
                    measureOnViewClicked(
                        event = viewClicked.event,
                        delegateOwner = resolvedDelegateOwner,
                        productDimensions = mapOf("item" to viewId)
                    )
                }
            }
        }

        component.value.findAnnotation<ViewFocused>()?.let { viewFocused ->

            for (viewId in viewFocused.viewIds) {
                val id = component.getIdentifier(viewId)
                val view = component.findViewById<View>(id)
                view.setOnFocusChangeListener { _, _ ->
                    measureOnViewFocused(
                        event = viewFocused.event,
                        delegateOwner = resolvedDelegateOwner,
                        productDimensions = mapOf("item" to viewId)
                    )
                }
            }
        }

    }
}

private class AnnotationProductMetricsDelegate(
    productMetrics: ProductMetrics,
    override val telemetryManager: TelemetryManager
) : ProductMetricsDelegate {
    override val productGroup: String = productMetrics.group
    override val productFlow: String = productMetrics.flow
    override val productDimensions: Map<String, String> = productMetrics.dimensions.toMap()
}

private inline fun <reified T : Annotation> Any.findAnnotation(): T? =
    javaClass.annotations.filterIsInstance<T>().firstOrNull()

@VisibleForTesting
internal fun <T> Array<T>.toMap(): Map<T, T> {
    require(size % 2 == 0) { "The array must have an even number of elements." }
    return toList().chunked(2).associate { it[0] to it[1] }
}
