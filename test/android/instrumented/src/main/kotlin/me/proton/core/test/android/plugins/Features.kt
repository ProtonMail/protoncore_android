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

package me.proton.core.test.android.plugins

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class Features constructor(private val host: String, private val proxyToken: String?) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var cachedFeatures: JSONArray? = null

    val paymentsAndroidDisabled: Boolean by lazy {
        getBooleanFeature("PaymentsAndroidDisabled")
    }

    private fun getBooleanFeature(featureCode: String): Boolean {
        val feature = getFeatures().findFeature(featureCode) ?: error("Could not find value for `$featureCode` flag.")

        check(feature.getString("Type") == "boolean") {
            "Unexpected feature type for `$featureCode` feature."
        }

        return feature.getBoolean("Value")
    }

    private fun JSONArray.findFeature(featureCode: String): JSONObject? {
        for (i in 0 until length()) {
            val feature = getJSONObject(i)
            if (feature.getString("Code") == featureCode) {
                return feature
            }
        }
        return null
    }

    private fun getFeatures(): JSONArray {
        cachedFeatures?.let { return it }

        return fetchFeatures().also {
            cachedFeatures = it
        }
    }

    private fun fetchFeatures(): JSONArray {
        val request = Request.Builder().apply {
            url("https://$host/core/v4/features")
            addHeader("x-pm-appversion", "android-mail@1.14.0")
            proxyToken?.let { addHeader("x-atlas-secret", it) }
        }.build()

        client.newCall(request).execute().use { response ->
            check(response.code == 200) { "Could not get feature flags: $response" }
            val body = JSONObject(response.body!!.string())
            return body.getJSONArray("Features")
        }
    }
}
