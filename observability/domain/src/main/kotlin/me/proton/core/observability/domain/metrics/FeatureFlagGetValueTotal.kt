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

package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Feature Flag GetValue.")
@SchemaId("https://proton.me/android_core_featureflag_getvalue_total_v1.schema.json")
@Deprecated("Please do not use. Kept for documentation.")
private data class FeatureFlagGetValueTotal(
    @Required override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(
        status: ValueStatus
    ) : this(LabelsData(status))

    @Serializable
    public data class LabelsData constructor(
        val status: ValueStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ValueStatus {
        enabled,
        disabled,
        unknown
    }

    public companion object {
        public val Enabled: FeatureFlagGetValueTotal = FeatureFlagGetValueTotal(ValueStatus.enabled)
        public val Disabled: FeatureFlagGetValueTotal = FeatureFlagGetValueTotal(ValueStatus.disabled)
        public val Unknown: FeatureFlagGetValueTotal = FeatureFlagGetValueTotal(ValueStatus.unknown)
    }
}
