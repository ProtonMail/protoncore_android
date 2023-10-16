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
 * The annotation can be applied to an activity or a fragment.
 * If applied, the telemetry data will be automatically collected
 * when the given screen is displayed.
 * @param event The telemetry event name.
 * @param dimensions Additional dimensions to use for this event.
 *  The array must have an event number of elements,
 *  which will be mapped to a `Map<String, String>`.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class ScreenDisplayed(
    public val event: String,
    public val dimensions: Array<String> = []
)
