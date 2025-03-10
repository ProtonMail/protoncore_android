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

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.fail
import kotlinx.coroutines.runBlocking
import me.proton.core.test.performance.LogcatFilter.Companion.clientPerformanceTag
import me.proton.core.test.performance.client.LokiClient
import me.proton.core.test.performance.measurement.Measurement
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

public class MeasurementProfile(
    private val workflow: String,
    measurementConfig: MeasurementConfig
) {
    private val appContext: Context by lazy { ApplicationProvider.getApplicationContext() }
    private var collectedLogcatLogs = mutableMapOf<String, String>()
    internal val measurements = mutableListOf<Measurement>()
    internal var logcatFilter: LogcatFilter = LogcatFilter()
    internal var sli: String? = null

    @VisibleForTesting
    internal var measuresList: MutableList<MeasureBlock> = mutableListOf()

    internal val sharedLabels = mutableMapOf(
        "workflow" to workflow,
        "build_type" to BuildConfig.BUILD_TYPE,
        "product" to appContext.packageName,
        "platform" to "android",
        "os_version" to "android ${Build.VERSION.RELEASE}",
        "device_model" to (Build.MODEL ?: "unknown")
    )

    // Shared StructuredMetadata
    internal val sharedMetadata = mutableMapOf(
        "app_version" to (appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: ""),
        "build_commit_sha1" to measurementConfig.buildCommitSha1,
        "environment" to measurementConfig.environment,
        "run_id" to measurementConfig.runId
    )

    public fun measure(block: () -> Unit) {
        // If id is set in LogcatFilter object then we will use it to correlate logs with metrics.
        sharedMetadata["id"] = (logcatFilter.id ?: UUID.randomUUID().toString())
        val measureBlock = MeasureBlock(this)
        try {
            Log.d(clientPerformanceTag, "Starting client measure block for workflow: $workflow.")
            measureBlock.startMeasurement()
            // Execute block which should be measured.
            block()
        } finally {
            measureBlock.stopMeasurement()
            Log.d(clientPerformanceTag, "Finished client measure block for workflow: $workflow.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun pushLogcatLogs(): MeasurementProfile = apply {

        val createStreamEntry: (String, String, JSONObject) -> JSONArray = { key, value, metrics ->
            JSONArray().put(key).put(value).put(metrics)
        }

        val lokiValuesJsonArray = JSONArray().apply {
            logcatFilter.getLogcatLogs().forEach { logLine ->
                put(createStreamEntry(logLine.key, logLine.value, JSONObject(sharedMetadata.toMap())))
            }
        }

        val logsPayload =
            JSONObject().put(
                "streams",
                JSONArray()
                    .put(
                        JSONObject().put(
                            "stream", JSONObject(sharedLabels.toMap())
                        ).put("values", lokiValuesJsonArray)
                    )
            )

        runBlocking {
            LokiClient.pushLokiEntry(logsPayload.toString())
        }
    }

    public fun clearLogcatLogs(): MeasurementProfile = apply {
        logcatFilter.clearLogcat()
        collectedLogcatLogs.clear()
    }

    public fun setServiceLevelIndicator(sli: String): MeasurementProfile = apply {
        this.sli = sli
    }

    public fun setLogcatFilter(logcatFilter: LogcatFilter): MeasurementProfile = apply {
        this.logcatFilter = logcatFilter
    }

    public fun addMetricToMeasures(key: String, value: String) {
        measuresList.forEach { measure ->
            measure.addMetric(key, value)
        }
    }

    public fun addMetric(data: Map<String, String>) {
        measuresList.forEach { measure ->
            measure.addMetric(data)
        }
    }

    internal fun getServiceLevelIndicator(): String? = sli

    public fun addMeasurement(measurement: Measurement): MeasurementProfile = apply { measurements.add(measurement) }

    internal fun getProfileMetricsStreams(): List<Any?> =
        measuresList.map { measure ->
            if (getServiceLevelIndicator() == null) {
                fail(
                    "MeasurementProfile: measure block for profile with workflow: " +
                        "\"${measure.profile.workflow}\" expected Service Level Indicator to be set via " +
                        "profile.setServiceLevelIndicator() but it wasn't. Current value is \"null\"."
                )
            } else {
                measure.getMeasureStream()
            }
        }
}
