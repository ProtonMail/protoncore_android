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

package me.proton.core.telemetry.presentation.annotation

/**
 * The annotation can be applied to activities or fragments.
 * NOTE: You activity/fragment should also implement
 * [me.proton.core.telemetry.presentation.UiComponentProductMetricsDelegateOwner] interface.
 * NOTE: This annotation cannot be used, if your delegate owner
 * returns a non-null [delegate][me.proton.core.telemetry.presentation.ProductMetricsDelegate].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class ProductMetrics(
    public val group: String,
    public val flow: String,
    public val dimensions: Array<String> = []
)
