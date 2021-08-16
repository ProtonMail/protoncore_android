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

package format

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SarifQualityReport(
    @SerialName("runs")
    val runs: List<Run>
) {

    @Serializable
    data class Run(
        @SerialName("results")
        val results: List<Result>
    ) {

        @Serializable
        data class Result(
            @SerialName("level")
            val level: String,
            @SerialName("locations")
            val locations: List<Location>,
            @SerialName("message")
            val message: Message,
            @SerialName("ruleId")
            val ruleId: String,
        ) {
            @Serializable
            data class Location(
                @SerialName("physicalLocation")
                val physicalLocation: Physical
            ) {

                @Serializable
                data class Physical(
                    @SerialName("artifactLocation")
                    val artifactLocation: ArtifactLocation,
                    @SerialName("region")
                    val region: Region,
                ) {
                    @Serializable
                    data class ArtifactLocation(
                        @SerialName("uri")
                        val uri: String
                    )

                    @Serializable
                    data class Region(
                        @SerialName("startColumn")
                        val startColumn: Int,
                        @SerialName("startLine")
                        val startLine: Int
                    )
                }
            }

            @Serializable
            data class Message(
                @SerialName("text")
                val text: String
            )
        }
    }
}