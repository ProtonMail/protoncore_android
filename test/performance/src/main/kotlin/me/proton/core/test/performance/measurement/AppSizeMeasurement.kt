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

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.test.core.app.ApplicationProvider
import me.proton.core.test.performance.MeasurementConfig
import me.proton.core.test.performance.MeasurementProfile
import java.io.File
import java.text.DecimalFormat

public class AppSizeMeasurement : Measurement {

    private val unknown = "unknown"
    private val appContext: Context by lazy { ApplicationProvider.getApplicationContext() }
    override fun onStartMeasurement(measurementProfile: MeasurementProfile) {
        measurementProfile.addMetricToMeasures("app_size", getBundleSize(appContext))
    }

    override fun onStopMeasurement(measurementProfile: MeasurementProfile) {
        // We already added "app_size" in onStartMeasurement()
    }

    @NonNull
    private fun getBundleSize(context: Context): String {
        return try {
            val appInfo =
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                ).applicationInfo
            val sourceDir = appInfo?.sourceDir
            val file = sourceDir?.let { File(it) }
            if (file?.exists() == true) {
                val sizeInBytes = file.length()
                val sizeInMB = sizeInBytes / (1024 * 1024).toDouble()
                val df = DecimalFormat("#.##")
                df.format(sizeInMB)
            } else {
                unknown
            }
        } catch (e: PackageManager.NameNotFoundException) {
            unknown
        }
    }
}
