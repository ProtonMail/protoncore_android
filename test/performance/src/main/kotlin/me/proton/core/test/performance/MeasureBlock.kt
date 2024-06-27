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

import android.annotation.SuppressLint
import android.util.Log
import junit.framework.TestCase.fail
import org.json.JSONArray
import org.json.JSONObject

public class MeasureBlock(
    internal val profile: MeasurementProfile
) {
    private val labels = mutableMapOf<String, Any>()
    private val metrics = mutableMapOf<String, Any>()
    private val metadata = mutableMapOf<String, Any>()

    init {
        labels.putAll(profile.sharedLabels)
        metadata.putAll(profile.sharedMetadata)
        labels["sli"] = profile.sli.toString()
    }

    @SuppressLint("VisibleForTests")
    internal fun startMeasurement() {
        // Add current ProfileMeasure object to profile measure list.
        profile.measuresList.add(this)
        profile.measurements.forEach { it.onStartMeasurement(profile) }
    }

    internal fun stopMeasurement(): MeasureBlock = apply {
        profile.measurements.forEach {
            it.onStopMeasurement(profile)
        }
    }

    // Get stream for each measure block.
    internal fun getMeasureStream(): JSONObject {

        Log.d(LogcatFilter.clientPerformanceTag, "Preparing measure stream.")

        val lokiStreamLabels = JSONObject(labels.toMap())
        val metricsJson = JSONObject(metrics.toMap())
        val metadataJson = JSONObject(metadata.toMap())
        val timestamp = System.currentTimeMillis() * 1000000

        val createStreamEntry: (String, String, JSONObject) -> JSONArray = { key, value, metrics ->
            JSONArray().put(key).put(value).put(metrics)
        }

        val lokiValuesJsonArray = JSONArray().apply {
            put(createStreamEntry(timestamp.toString(), metricsJson.toString(), metadataJson))
        }

        return JSONObject().put("stream", lokiStreamLabels).put("values", lokiValuesJsonArray)
    }

    internal fun addLabel(key: String, value: String) {
        labels[key] = value
    }

    internal fun addLabels(data: Map<String, String>): Unit = this.labels.putAll(data)

    public fun addMetric(key: String, value: String) {
        validateMetricsSize { metrics[key] = value }
    }

    public fun addMetric(data: Map<String, String>) {
        validateMetricsSize { this.metrics.putAll(data) }
    }

    public fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    public fun addMetadata(data: Map<String, String>): Unit = this.metadata.putAll(data)

    private fun validateMetricsSize(block: () -> Unit) {
        if (metrics.size <= 10) {
            block()
        } else {
            fail(
                "MeasureBlock: you have exceeded the maximum metrics size count. " +
                    "For performance reasons it is not allowed to push more than 10 metrics."
            )
        }
    }
}
