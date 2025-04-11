/*
 * Copyright (c) 2025 Proton AG
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
@Schema(description = ".")
@SchemaId("https://proton.me/android_core_edm_post_login_total_v1.schema.json")
public class EdmPostLoginTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(status: PostLoginStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData(
        val status: PostLoginStatus
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class PostLoginStatus {
        accountReady,
        needChooseUsername,
        needDeviceSecret,
        needSecondFactor,
        needTwoPassMode,
        needChangePassword,
        unlockPrimaryKeyError,
        userCheckError
    }
}
