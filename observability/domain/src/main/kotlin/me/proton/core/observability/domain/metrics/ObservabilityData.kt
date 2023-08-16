/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import java.net.URI

@Suppress("PropertyName", "VariableNaming")
@Serializable
public sealed class ObservabilityData {
    @get:Schema(required = true)
    public abstract val Labels: Any

    @get:Schema(minimum = "1", required = true)
    public abstract val Value: Long

    /** Extracts the metric name from [SchemaId] annotation. */
    public val metricName: String get() = getMetricName(this::class.java)

    /** Extracts the metric version from [SchemaId] annotation. */
    public val metricVersion: Long get() = getMetricVersion(this::class.java)

    public companion object {
        private fun getSchemaIdUri(klass: Class<out ObservabilityData>): URI {
            val schemaId = requireNotNull(klass.getAnnotation(SchemaId::class.java)) {
                "Missing SchemaId annotation for metric: ${klass.name}."
            }
            return URI.create(schemaId.id)
        }

        public fun getMetricName(klass: Class<out ObservabilityData>): String {
            return getSchemaIdUri(klass).path
                .removePrefix("/")
                .replace(Regex("_v\\d+\\.schema\\.json"), "")
        }

        public fun getMetricVersion(klass: Class<out ObservabilityData>): Long {
            val schemaIdUri = getSchemaIdUri(klass)
            val match = Regex(".*_v(\\d+)\\.schema\\.json$").find(schemaIdUri.path)
            return match?.groupValues?.get(1)?.toLong() ?: error("Could not parse metric version (uri=$schemaIdUri).")
        }
    }
}
