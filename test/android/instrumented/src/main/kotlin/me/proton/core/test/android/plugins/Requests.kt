/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.plugins

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.Serializable
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag
import me.proton.core.util.kotlin.deserialize
import java.net.HttpURLConnection
import java.net.URL

object Requests {
    @Serializable
    data class InternalApi(
        val baseUrl: String,
        val endpoints: LinkedHashMap<InternalApiEndpoint, String>
    )

    enum class InternalApiEndpoint {
        JAIL_UNBAN
    }

    private const val internalApiJsonPath: String = "sensitive/internal_apis.json"

    private val internalApi: InternalApi = InstrumentationRegistry
        .getInstrumentation()
        .context
        .assets
        .open(internalApiJsonPath)
        .bufferedReader()
        .use { it.readText() }
        .deserialize()

    fun request(endpoint: String, method: String) {
        val url = "https://${internalApi.baseUrl}$endpoint"
        Log.d(testTag, "Sending $method request to $endpoint")
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = method
        }
    }

    fun jailUnban() = internalApi.endpoints[InternalApiEndpoint.JAIL_UNBAN]?.let { request(it, "GET") }
}
