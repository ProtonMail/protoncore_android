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

import android.view.View
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import me.proton.core.presentation.ui.view.AdditionalOnClickListener
import me.proton.core.presentation.ui.view.AdditionalOnFocusChangeListener
import me.proton.core.presentation.ui.view.ProtonMaterialToolbar
import me.proton.core.presentation.utils.UiComponent
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegate.Companion.KEY_ITEM
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.annotation.MenuItemClicked
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused
import me.proton.core.telemetry.presentation.measureOnScreenClosed
import me.proton.core.telemetry.presentation.measureOnScreenDisplayed
import me.proton.core.telemetry.presentation.measureOnViewClicked
import me.proton.core.telemetry.presentation.measureOnViewFocused
import me.proton.core.telemetry.presentation.setupViewMetrics
import javax.inject.Inject

public class SetupProductMetrics @Inject constructor(
    private val telemetryManager: TelemetryManager
) {
    /**
     * This method should be invoked for any activity/fragment
     * that wants to use the product metrics
     * [annotations][me.proton.core.telemetry.presentation.annotation].
     * If you don't use the annotations, you don't have to call this.
     */
    public operator fun invoke(
        lifecycleOwner: LifecycleOwner,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        component: UiComponent
    ) {
        val delegateOwner = component.value as? ProductMetricsDelegateOwner
        val delegate = delegateOwner?.productMetricsDelegate
        val productMetrics = component.value.findAnnotation<ProductMetrics>()
        val hasProductMetricsAnnotation = component.value.hasProductMetricsAnnotation()

        when {
            delegate != null && productMetrics != null -> error(
                "Cannot use both the ${ProductMetricsDelegate::class.simpleName} and " +
                        "${ProductMetrics::class.simpleName} annotation in ${component.value::class.qualifiedName}."
            )

            delegate == null && productMetrics == null && hasProductMetricsAnnotation -> error(
                "${component.value::class.qualifiedName} must implement either implement " +
                        "${ProductMetricsDelegateOwner::class.simpleName} and " +
                        "provide a non-null ${ProductMetricsDelegate::class.simpleName}, or " +
                        "annotate itself with a ${ProductMetrics::class.simpleName} annotation."
            )

            delegate == null && productMetrics == null -> return
        }

        val resolvedDelegate = when {
            delegate != null -> delegate
            productMetrics != null -> AnnotationProductMetricsDelegate(
                productMetrics,
                telemetryManager
            )

            else -> error("Fatal error: both delegateOwner and screenMetrics and null.")
        }

        component.value.findAnnotation<ScreenDisplayed>()?.let { screenDisplayed ->
            measureOnScreenDisplayed(
                event = screenDisplayed.event,
                dimensions = screenDisplayed.dimensions.toMap(),
                delegate = resolvedDelegate,
                lifecycleOwner = lifecycleOwner,
                savedStateRegistryOwner = savedStateRegistryOwner,
                priority = screenDisplayed.priority
            )
        }

        component.value.findAnnotation<ScreenClosed>()?.let { screenClosed ->
            measureOnScreenClosed(
                event = screenClosed.event,
                dimensions = screenClosed.dimensions.toMap(),
                delegate = resolvedDelegate,
                lifecycleOwner = lifecycleOwner,
                onBackPressedDispatcherOwner = onBackPressedDispatcherOwner,
                priority = screenClosed.priority
            )
        }

        component.value.findAnnotation<ViewClicked>()?.let { viewClicked ->
            setupViewClicked(component, lifecycleOwner, resolvedDelegate, viewClicked)
        }

        component.value.findAnnotation<ViewFocused>()?.let { viewFocused ->
            setupViewFocused(component, lifecycleOwner, resolvedDelegate, viewFocused)
        }

        component.value.findAnnotation<MenuItemClicked>()?.let { menuItemClicked ->
            setupMenuItemClicked(component, lifecycleOwner, menuItemClicked, resolvedDelegate)
        }
    }

    private fun setupViewClicked(
        component: UiComponent,
        lifecycleOwner: LifecycleOwner,
        resolvedDelegate: ProductMetricsDelegate,
        viewClicked: ViewClicked
    ) = lifecycleOwner.setupViewMetrics {
        for (viewId in viewClicked.viewIds) {
            val id = component.getIdentifier(viewId)
            val view = component.findViewById<View>(id)

            view?.setOnClickListener(object : AdditionalOnClickListener {
                override fun onClick(p0: View?) {
                    measureOnViewClicked(
                        event = viewClicked.event,
                        delegate = resolvedDelegate,
                        dimensions = mapOf("item" to viewId),
                        priority = viewClicked.priority
                    )
                }
            })
        }
    }

    private fun setupViewFocused(
        component: UiComponent,
        lifecycleOwner: LifecycleOwner,
        resolvedDelegate: ProductMetricsDelegate,
        viewFocused: ViewFocused
    ) = lifecycleOwner.setupViewMetrics {
        for (viewId in viewFocused.viewIds) {
            val id = component.getIdentifier(viewId)
            val view = component.findViewById<View>(id)

            view?.onFocusChangeListener = object : AdditionalOnFocusChangeListener {
                override fun onFocusChange(view: View?, hasFocus: Boolean) {
                    if (hasFocus) {
                        measureOnViewFocused(
                            event = viewFocused.event,
                            delegate = resolvedDelegate,
                            dimensions = mapOf(KEY_ITEM to viewId),
                            priority = viewFocused.priority
                        )
                    }
                }
            }
        }
    }

    private fun setupMenuItemClicked(
        component: UiComponent,
        lifecycleOwner: LifecycleOwner,
        menuItemClicked: MenuItemClicked,
        resolvedDelegate: ProductMetricsDelegate
    ) = lifecycleOwner.setupViewMetrics {
        val toolbarId = component.getIdentifier(menuItemClicked.toolbarId)
        val toolbar = component.findViewById<View>(toolbarId) as? ProtonMaterialToolbar
        val itemIds = menuItemClicked.itemIds.associateBy { component.getIdentifier(it) }
        toolbar?.setAdditionalOnMenuItemClickListener {
            val itemIdName = itemIds[it.itemId] ?: return@setAdditionalOnMenuItemClickListener false
            measureOnViewClicked(
                event = menuItemClicked.event,
                delegate = resolvedDelegate,
                dimensions = mapOf("item" to itemIdName),
                priority = menuItemClicked.priority
            )
            true
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

private inline fun Any.hasProductMetricsAnnotation(): Boolean =
    javaClass.annotations.filterIsInstance<ScreenDisplayed>().firstOrNull() != null ||
            javaClass.annotations.filterIsInstance<ScreenClosed>().firstOrNull() != null ||
            javaClass.annotations.filterIsInstance<ViewClicked>().firstOrNull() != null ||
            javaClass.annotations.filterIsInstance<ViewFocused>().firstOrNull() != null ||
            javaClass.annotations.filterIsInstance<MenuItemClicked>().firstOrNull() != null

@VisibleForTesting
internal fun <T> Array<T>.toMap(): Map<T, T> {
    require(size % 2 == 0) { "The array must have an even number of elements." }
    return toList().chunked(2).associate { it[0] to it[1] }
}
