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

package me.proton.core.observability.domain.entity

import java.net.URI

/** Common interface for observability metrics data.
 * Each subclass should be marked with [SchemaId] annotation.
 */
public interface ObservabilityData {
    private val schemaIdUri: URI
        get() {
            val schemaId = requireNotNull(this::class.java.getAnnotation(SchemaId::class.java)) {
                "Missing ${SchemaId::class.simpleName} annotation for metric: ${this::class.simpleName}."
            }
            return URI.create(schemaId.id)
        }

    /** Extracts the metric name from [SchemaId] annotation. */
    public val metricName: String
        get() = schemaIdUri.path
            .removePrefix("/")
            .replace(Regex("_v\\d+\\.schema\\.json"), "")

    /** Extracts the metric version from [SchemaId] annotation. */
    public val metricVersion: Long
        get() {
            val match = Regex(".*_v(\\d+)\\.schema\\.json$").find(schemaIdUri.path)
            return match?.groupValues?.get(1)?.toLong() ?: error("Could not parse metric version (uri=$schemaIdUri).")
        }
}
