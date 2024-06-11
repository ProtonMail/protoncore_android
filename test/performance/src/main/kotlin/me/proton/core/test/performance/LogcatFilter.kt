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
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import java.io.IOException

public class LogcatFilter {
    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val tagLevels = mutableMapOf<String, LogcatLevel>()
    internal var shouldFailTestOnEmptyLogs = false
    internal var id: String?  = null

    public fun addTag(tag: String, level: LogcatLevel = LogcatLevel.VERBOSE): LogcatFilter = apply {
        tagLevels[tag] = level
    }

    public fun setLokiLogsId(id: String): LogcatFilter = apply { this.id = id }
    public fun failTestOnEmptyLogs(): LogcatFilter = apply { shouldFailTestOnEmptyLogs = true }

    private fun build(): String {
        val commandParts = mutableListOf<String>()

        commandParts.add("logcat -d -v epoch,printable,UTC,usec ")

        // Use --pid packageName parameter if tag is not given
        if (tagLevels.isNotEmpty()) {
            // Add specific tags with their levels
            tagLevels.forEach { (tag, _) ->
                commandParts.add("-s $tag")
            }
        } else {
            commandParts.add("| grep ${appContext.packageName}")
        }

        // Join all parts into a single command string
        return commandParts.joinToString(" ")
    }

    // Function to read Logcat logs for the specific tag(s) and log level(s)
    internal fun getLogcatLogs(): MutableMap<String, String> {
        val logs = mutableMapOf<String, String>()
        val command = build()

        try {
            // Construct the logcat command with filters for log level and tag
            val process = Runtime.getRuntime().exec(command)
            var logMessage = ""

            process.inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val timestampNano = getTimestampFromLogcat(line)
                    tagLevels.forEach tagLoop@{ (tag, _) ->
                        if (line.contains(tag)) {
                            logMessage = line.substringAfter("$tag: ").trim()
                            return@tagLoop
                        }
                    }
                    logs[timestampNano] = logMessage
                }
            }
        } catch (e: Exception) {
            Log.e(clientPerformanceTag, "Error getting logcat log for given tags. Please check the error below.")
            e.printStackTrace()
        }
        return logs
    }

    internal fun clearLogcat() {
        try {
            Runtime.getRuntime().exec("logcat -c")
        } catch (e: IOException) {
            Log.e(clientPerformanceTag, "Failed to clear logcat buffer: ${e.message}")
        }
    }

    private fun getTimestampFromLogcat(line: String): String {
        return line
            .trim()
            .split(" ")[0]
            .replace(".", "")
            // Loki expects nano seconds, so we have to add additional 0
            .plus("000")
    }

    public companion object {
        internal var clientPerformanceTag: String = "ProtonClientPerformance"
    }
}

public enum class LogcatLevel(public val code: String) {
    INFO("I"),
    DEBUG("D"),
    VERBOSE("V"),
    ERROR("E"),
    WARNING("W");

    override fun toString(): String = code
}