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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.presentation.utils.OnUiComponentCreatedListener
import me.proton.core.presentation.utils.UiComponent
import me.proton.core.telemetry.presentation.usecase.SetupProductMetrics

/**
 * The interface for providing the [ProductMetricsDelegate].
 */
public interface ProductMetricsDelegateOwner {
    /**
     * If your activity or fragment also uses the
     * [me.proton.core.telemetry.presentation.annotation.ProductMetrics] annotation,
     * then you MUST return `null` (to avoid duplicate product metric definitions).
     * If you don't use the annotation, you SHOULD return a non-null value.
     */
    public val productMetricsDelegate: ProductMetricsDelegate? get() = null
}

/**
 * The interface can be applied to activities or fragments.
 * This interface will make sure to call [SetupProductMetrics] (to handle the product metrics annotations).
 *
 * NOTE: the activity/fragment must call [onUiComponentCreated] in its `onCreate` method.
 * If you use [me.proton.core.presentation.ui.ProtonActivity], [me.proton.core.presentation.ui.ProtonDialogFragment],
 * or [me.proton.core.presentation.ui.ProtonFragment], then it's already called for you.
 */
public interface UiComponentProductMetricsDelegateOwner : ProductMetricsDelegateOwner, OnUiComponentCreatedListener {
    override fun onUiComponentCreated(
        lifecycleOwner: LifecycleOwner,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        component: UiComponent
    ) {
        super.onUiComponentCreated(lifecycleOwner, onBackPressedDispatcherOwner, savedStateRegistryOwner, component)
        val appContext = when (component) {
            is UiComponent.UiActivity -> component.activity.applicationContext
            is UiComponent.UiFragment -> component.fragment.requireContext().applicationContext
        }
        EntryPointAccessors
            .fromApplication<ProductMetricsInitializerEntryPoint>(appContext)
            .setupProductMetrics(lifecycleOwner, onBackPressedDispatcherOwner, savedStateRegistryOwner, component)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ProductMetricsInitializerEntryPoint {
    val setupProductMetrics: SetupProductMetrics
}

public fun ProductMetricsDelegateOwner(delegate: ProductMetricsDelegate): ProductMetricsDelegateOwner =
    object : ProductMetricsDelegateOwner {
        override val productMetricsDelegate: ProductMetricsDelegate = delegate
    }
