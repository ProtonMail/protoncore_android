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

package me.proton.core.test.kotlin

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

fun MockWebServer.enqueueFromResourceFile(filePath: String, classLoader: ClassLoader?) {
    val responseBody = loadFileContents(filePath, classLoader) ?: error("Could not load file: $filePath")
    val response = MockResponse()
        .setResponseCode(200)
        .setBody(responseBody)
        .addHeader("Content-Type", "application/json")
    enqueue(response)
}

private fun loadFileContents(path: String, classLoader: ClassLoader?): String? {
    return classLoader?.getResourceAsStream(path)?.bufferedReader()?.use { reader ->
        reader.readText()
    }
}
