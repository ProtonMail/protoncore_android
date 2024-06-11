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
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import me.proton.core.test.performance.LogcatFilter.Companion.clientPerformanceTag
import me.proton.core.test.performance.client.LokiClient
import org.json.JSONArray
import org.json.JSONObject


public class MeasurementContext {

    internal var testMethodName: String = ""
    internal var measurementConfig = MeasurementConfig
    private val measurementProfiles = mutableListOf<MeasurementProfile>()


    @RequiresApi(Build.VERSION_CODES.O)
    public fun setWorkflow(workflow: String): MeasurementProfile {
        val profile = MeasurementProfile(workflow, this.measurementConfig)
        measurementProfiles.add(profile)
        return profile
    }

    internal fun setMeasurementConfig(measurementConfig: MeasurementConfig): MeasurementContext = apply {
        this.measurementConfig = measurementConfig
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun uploadMetricsToLoki() {
        val payload = JSONObject().apply {
            put("streams", JSONArray().apply {
                measurementProfiles.forEach { profile ->
                    profile.getProfileMetricsStreams().filterNotNull().forEach { stream ->
                            put(stream)
                        }
                }
            })
        }
        Log.d(clientPerformanceTag, "Done with stream preparation.")
        Log.d(clientPerformanceTag, "Payload:\n${payload}")
        runBlocking {
            LokiClient.pushLokiEntry(payload.toString())
        }
    }

    @SuppressLint("VisibleForTests")
    public fun addMetric(key: String, value: String) {
        measurementProfiles.forEach { profile ->
            profile.addMetricToMeasures(key, value)
        }
    }

    @SuppressLint("VisibleForTests")
    public fun addMetadata(key: String, value: String) {
        measurementProfiles.forEach { profile ->
            profile.measuresList.forEach { measure ->
                measure.addMetadata(key, value)
            }
        }
    }
}
