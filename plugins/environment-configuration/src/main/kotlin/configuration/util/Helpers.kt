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

package configuration.util

import java.util.concurrent.TimeUnit

fun getTokenFromCurl(proxyUrl: String): String {

    val process = ProcessBuilder("curl", proxyUrl)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    try {
        val error: String = process.errorStream.bufferedReader().readText()
        val output = process.inputStream.bufferedReader().readText()

        // Avoid potential hangs
        require(process.waitFor(1, TimeUnit.SECONDS)) {
            "Curl process timed out."
        }

        require(process.exitValue() == 0) {
            "Could not obtain proxy token with exit code ${process.exitValue()}: $error"
        }

        require(Regex("^[a-zA-Z0-9]+$").matches(output)) {
            "Invalid token format. Expected a single word, got: $output"
        }

        return output.trim()
    } finally {
        process.destroy()
    }
}


fun Any?.toBuildConfigValue(): String {
    return when (this) {
        null -> "null"
        is Boolean -> "$this"
        is String -> "\"$this\""
        else -> throw IllegalArgumentException("Unknown build field conversion for type ${this::class.java.simpleName}")
    }
}
