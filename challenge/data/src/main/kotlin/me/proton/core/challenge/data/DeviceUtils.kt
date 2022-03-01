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
import android.os.storage.StorageManager
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID

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

@RequiresApi(Build.VERSION_CODES.O)
fun Context.deviceStorage(): Double {
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    val storageVolumes = storageManager.storageVolumes
    var totalStorage = 0.0
    for (storageVolume in storageVolumes) {
        val uuidStr = storageVolume.uuid
        val uuid: UUID = if (uuidStr == null) StorageManager.UUID_DEFAULT else UUID.fromString(uuidStr)
        totalStorage += storageStatsManager.getTotalBytes(uuid)
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
