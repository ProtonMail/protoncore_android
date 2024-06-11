/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.performance.measurement

import me.proton.core.test.performance.MeasurementProfile


/**
 * Declaring measurement example:
 *
 * class MeasurementSample : Measurement {
 *     override fun onStartMeasurement(context: MeasurementContext) {
 *         // Start measuring metric on start
 *     }
 *
 *     override fun onStopMeasurement(measurementContext: MeasurementContext) {
 *         // Calculate metric on stop and add it to the list of metrics.
 *         measurementContext.addMetric("custom_metric", customMetric)
 *     }
 * }
 *
 * Usage in tests:
 *
 * @Test
 * fun testPerformMeasurement() {
 *     val measurementProfile = measurementContext
 *         .addMeasurement(MeasurementSample())
 *         .setWorkflow("sample_workflow")
 *
 *     measurementProfile.measure {
 *         // Your code here
 *     }
 * }
 *
 * An interface which provides measurement hooks.
 */
public interface Measurement {
    public fun onStartMeasurement(measurementProfile: MeasurementProfile)
    public fun onStopMeasurement(measurementProfile: MeasurementProfile)
}