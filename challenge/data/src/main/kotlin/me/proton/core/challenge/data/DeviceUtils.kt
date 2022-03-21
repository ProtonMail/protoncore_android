/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.challenge.data

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val MILLIS_IN_MINUTE = 60_1000

fun deviceModelName(): String = Build.MODEL

fun deviceUID(): String = Settings.Secure.ANDROID_ID

fun appLanguage(): String = Locale.getDefault().language

fun deviceTimezone(): String = TimeZone.getDefault().id

/**
 * Returns the offset, measured in minutes.
 */
fun deviceTimezoneOffset(): Int {
    val calendar = Calendar.getInstance(Locale.getDefault())
    return -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / MILLIS_IN_MINUTE
}

fun Context.deviceRegion(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    resources.configuration.locales[0].country
} else {
    resources.configuration.locale.country
}

fun isDeviceRooted() = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()

fun Context.deviceFontSize() = resources.configuration.fontScale

fun Context.defaultDeviceInputMethod() =
    Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)

fun Context.deviceInputMethods(): List<String> {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val inputMethodProperties = imm.enabledInputMethodList

    return inputMethodProperties.map { it.id }
}

fun Context.nightMode(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.deviceStorage(): Double {
    val totalBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        deviceVolumesStorage()
    } else {
        val stat = StatFs(Environment.getDataDirectory().path)
        (stat.blockCountLong * stat.blockSizeLong).toDouble()
    }
    return totalBytes / 1_000_000_000 // in GB
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.deviceVolumesStorage(): Double {
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val extDirs = getExternalFilesDirs(null)
    var totalStorage = 0.0

    extDirs.forEach { file ->
        val storageVolume = storageManager.getStorageVolume(file)
        if (storageVolume != null) {
            totalStorage += if (storageVolume.isPrimary) {
                val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            } else {
                file.totalSpace
            }
        }
    }
    return totalStorage
}

private fun checkRootMethod1(): Boolean {
    val buildTags = Build.TAGS
    return buildTags != null && buildTags.contains("test-keys")
}

private fun checkRootMethod2(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )
    for (path in paths) {
        if (File(path).exists()) return true
    }
    return false
}

private fun checkRootMethod3(): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val stream = BufferedReader(InputStreamReader(process.inputStream))
        stream.readLine() != null
    } catch (t: Throwable) {
        false
    } finally {
        process?.destroy()
    }
}
