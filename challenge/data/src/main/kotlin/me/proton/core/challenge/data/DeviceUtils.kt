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
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

private const val MILLIS_IN_MINUTE = 60_000
private const val BYTES_GB = 1_000_000_000

public fun deviceModelName(): Long =
    String.format(
        Locale.US,
        "%s/%s %s",
        Build.MODEL,
        Build.BRAND,
        Build.DEVICE
    ).rollingHash()

public fun Context.deviceUID(): String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

public fun appLanguage(): String = LocaleListCompat.getDefault()[0].language

public fun deviceTimezone(): String = TimeZone.getDefault().id

/**
 * Returns the offset, measured in minutes.
 */
public fun deviceTimezoneOffset(): Int {
    val calendar = Calendar.getInstance(Locale.getDefault())
    return -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / MILLIS_IN_MINUTE
}

public fun Context.deviceRegion(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    resources.configuration.locales[0].country
} else {
    resources.configuration.locale.country
}

public suspend fun isDeviceRooted(): Boolean = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()

public fun Context.deviceFontSize(): Float = resources.configuration.fontScale

public fun Context.defaultDeviceInputMethod(): String =
    Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)

public fun Context.deviceInputMethods(): List<String> {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val inputMethodProperties = imm.enabledInputMethodList

    return inputMethodProperties.map { it.packageName }
}

public fun Context.nightMode(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

public suspend fun Context.deviceStorage(): Double = withContext(Dispatchers.IO) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        (deviceVolumesStorage() / BYTES_GB).toDoubleRound()
    } else {
        val stat = StatFs(Environment.getDataDirectory().path)
        (stat.blockCountLong * stat.blockSizeLong / BYTES_GB).toDoubleRound()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Context.deviceVolumesStorage(): Long {
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val externalDirs = getExternalFilesDirs(null)
    var totalStorage: Long = 0

    externalDirs.forEach { dir ->
        val storageVolume = storageManager.getStorageVolume(dir)
        if (storageVolume != null) {
            totalStorage += if (storageVolume.isPrimary) {
                val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            } else {
                dir.totalSpace
            }
        }
    }
    return totalStorage
}

private fun checkRootMethod1(): Boolean {
    val buildTags = Build.TAGS
    return buildTags != null && buildTags.contains("test-keys")
}

private suspend fun checkRootMethod2(): Boolean = withContext(Dispatchers.IO) {
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
    paths.any { File(it).exists() }
}

private suspend fun checkRootMethod3(): Boolean = withContext(Dispatchers.IO) {
    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val stream = BufferedReader(InputStreamReader(process.inputStream))
        stream.readLine() != null
    } catch (t: Throwable) {
        false
    } finally {
        process?.destroy()
    }
}

private fun Long.toDoubleRound(): Double = (this * 100.0).roundToInt() / 100.0

/**
 * Rolling hash a string via sliding window.
 * Returns a hash of the string.
 *
 * Based on the BE requirements.
 */
private fun String.rollingHash(base: Long = 7, mod: Long = 100000007): Long {
    var answer: Long = 0
    var coefficient: Long = 0
    for (code in this.toByteArray(Charsets.UTF_8)) {
        coefficient = if (coefficient == 0L) {
            1L
        } else {
            coefficient * base % mod
        }
        answer += code * coefficient
    }
    return answer
}
