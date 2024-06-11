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

public class DurationMeasurement : Measurement {

    private var startTime: Long = 0L
    private var stopTime: Long = 0L
    private val elapsedTime: Long get() = stopTime - startTime

    override fun onStartMeasurement(measurementProfile: MeasurementProfile) {
        startTime = System.currentTimeMillis()
    }

    override fun onStopMeasurement(measurementProfile: MeasurementProfile) {
        stopTime = System.currentTimeMillis()
        measurementProfile.addMetricToMeasures("duration", "%.2f".format(elapsedTime / 1000.0))
    }
}
