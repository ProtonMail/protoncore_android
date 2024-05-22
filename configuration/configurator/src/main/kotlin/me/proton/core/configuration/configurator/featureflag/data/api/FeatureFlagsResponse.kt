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

package me.proton.core.configuration.configurator.featureflag.data.api

import kotlinx.serialization.Serializable

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