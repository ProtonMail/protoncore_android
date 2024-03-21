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
@Schema(description = "Feature Flag Await.")
@SchemaId("https://proton.me/android_core_featureflag_await_total_v1.schema.json")
@Deprecated("Will be removed when CredentialLessDisabled FF will be removed. Keep for documentation.")
public data class FeatureFlagAwaitTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    internal constructor(status: AwaitStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData constructor(
        val status: AwaitStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class AwaitStatus {
        success,
        failure
    }

    public companion object {
        public val Success: FeatureFlagAwaitTotal = FeatureFlagAwaitTotal(AwaitStatus.success)
        public val Failure: FeatureFlagAwaitTotal = FeatureFlagAwaitTotal(AwaitStatus.failure)
    }
}