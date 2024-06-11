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

package me.proton.core.test.performance

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import me.proton.core.test.performance.LogcatFilter.Companion.clientPerformanceTag
import me.proton.core.test.performance.annotation.Measure
import org.junit.rules.TestWatcher
import org.junit.runner.Description

public class MeasurementRule : TestWatcher() {

    private var measurementContext: MeasurementContext = MeasurementContext()

    public fun measurementContext(measurementConfig: MeasurementConfig): MeasurementContext =
        measurementContext.setMeasurementConfig(measurementConfig)

    override fun starting(description: Description?) {
        super.starting(description)
        val annotation = description?.annotations?.find { it is Measure } as? Measure
        if (annotation != null) {
            Log.d(clientPerformanceTag, "STARTING test ${description.methodName}, annotated with @Measure.")
            measurementContext.testMethodName = description.methodName
        }
    }

    override fun failed(e: Throwable?, description: Description?) {
        super.failed(e, description)
        val annotation = description?.annotations?.find { it is Measure } as? Measure
        if (annotation != null) {
            Log.d(clientPerformanceTag, "FAILURE: test ${description.methodName}, annotated with @Measure.")
            measurementContext.addMetric("status", "failed")
        }
    }

    override fun succeeded(description: Description?) {
        super.succeeded(description)
        val annotation = description?.annotations?.find { it is Measure } as? Measure
        if (annotation != null) {
            Log.d(clientPerformanceTag, "SUCCESS: test ${description.methodName}, annotated with @Measure.")
            measurementContext.addMetric("status", "succeeded")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun finished(description: Description?) {
        super.finished(description)
        val annotation = description?.annotations?.find { it is Measure } as? Measure
        if (annotation != null) {
            measurementContext.addMetadata("test", description.methodName)
            Log.d(clientPerformanceTag, "FINISHED test ${description.methodName}, annotated with @Measure.")

            measurementContext.uploadMetricsToLoki()

        }
    }
}