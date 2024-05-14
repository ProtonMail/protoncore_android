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

package me.proton.core.configuration.configurator.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.proton.core.configuration.configurator.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

class FeatureFlagsViewModel : ViewModel() {
    private val _featureFlags = MutableStateFlow<List<Feature>>(emptyList())
    val featureFlags: StateFlow<List<Feature>> = _featureFlags.asStateFlow()
    private var currentProject: String? = null

    fun loadFeatureFlagsByProject(project: String) = viewModelScope.launch {
        currentProject = project
        val flags = FeatureFlagsCacheManager.getFeatureFlags()
        _featureFlags.value = flags.filter { it.project == project }
    }
}

interface FeatureFlagsApi {
    @GET("api/client/features")
    suspend fun getFeatureFlags(@Header("Authorization") apiToken: String): FeatureFlagsResponse
}


object RetrofitInstance {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.UNLEASH_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val api: FeatureFlagsApi by lazy {
        retrofit.create(FeatureFlagsApi::class.java)
    }
}

object FeatureFlagsCacheManager {
    private var cache: List<Feature>? = null
    private var lastFetchTime: Long? = null
    private val cacheTimeInterval = 60000 // Cache validity duration in milliseconds

    suspend fun getFeatureFlags(): List<Feature> {
        return withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            if (lastFetchTime != null && (currentTime - lastFetchTime!!) < cacheTimeInterval && cache != null) {
                return@withContext cache!!
            }

            try {
                val apiToken = BuildConfig.UNLEASH_API_TOKEN
                if (apiToken.isNotEmpty()) {
                    val response = RetrofitInstance.api.getFeatureFlags(apiToken)
                    cache = response.features
                    lastFetchTime = currentTime
                    return@withContext response.features
                } else {
                    return@withContext emptyList<Feature>()
                }
            } catch (e: Exception) {
                return@withContext emptyList<Feature>()
            }
        }
    }
}

@Serializable
data class FeatureFlagsResponse(val version: Int, val features: List<Feature>, val query: Query, val meta: Meta)

@Serializable
data class Feature(
    val name: String,
    val type: String,
    val enabled: Boolean,
    val project: String,
    val stale: Boolean,
    val strategies: List<Strategy>,
    val variants: List<Variant>,
    val description: String?,
    val impressionData: Boolean,
    val dependencies: List<Dependency>? = emptyList()
) {
    fun isEnabled100(): Boolean = strategies.singleOrNull()?.parameters?.rollout == "100"
    fun rolloutPercentage(): Int? = strategies.singleOrNull()?.parameters?.rollout?.toIntOrNull()
    fun strategiesDescription(): String {
        if (strategies.isEmpty()) return "No strategies"

        return strategies.joinToString("\n\n") { strategy ->
            val rolloutPercentage: Int = strategy.parameters.rollout?.toIntOrNull() ?: 0
            val visualRepresentation = when {
                rolloutPercentage == 100 -> "[Fully Enabled]"
                rolloutPercentage > 0 -> {
                    "[" + "▓".repeat(rolloutPercentage / 10) + "░".repeat(10 - rolloutPercentage / 10) + "]"
                }

                else -> "[" + "░".repeat(10) + "]"
            }

            buildString {
                append("Strategy Name: ${strategy.name}, Rollout: $rolloutPercentage%\n")
                append("Visual: $visualRepresentation\n")

                for (constraint in strategy.constraints) {
                    append("Constraint: ${constraint.contextName} ${constraint.operation} ")
                    append(constraint.values?.joinToString(", ") ?: "N/A")
                    append("\n")
                }
            }
        }
    }

}

@Serializable
data class Strategy(val name: String, val constraints: List<Constraint>, val parameters: Parameters) {
    fun description(): String = "Strategy Name: $name, Rollout: ${parameters.rollout ?: "0"}%"
}

@Serializable
data class Constraint(
    val values: List<String>?,
    val inverted: Boolean,
    val operation: String = "defaultOperation",
    val contextName: String,
    val caseInsensitive: Boolean
)

@Serializable
data class Parameters(val groupId: String? = null, val rollout: String? = null, val stickiness: String? = null)

@Serializable
data class Variant(
    val name: String,
    val weight: Int,
    val payload: Payload,
    val stickiness: String,
    val weightType: String
)

@Serializable
data class Payload(val type: String, val value: String)

@Serializable
data class Dependency(val feature: String, val enabled: Boolean, val variants: List<String>?)

@Serializable
data class Query(val environment: String, val inlineSegmentConstraints: Boolean)

@Serializable
data class Meta(val revisionId: Int, val etag: String, val queryHash: String)

