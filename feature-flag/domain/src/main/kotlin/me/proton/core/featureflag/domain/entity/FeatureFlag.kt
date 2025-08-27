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

package me.proton.core.featureflag.domain.entity

import me.proton.core.domain.entity.UserId

public data class FeatureFlag(
    val userId: UserId?,
    val featureId: FeatureId,
    val scope: Scope,
    val defaultValue: Boolean,
    val value: Boolean,
    val variantName: String?,
    val payloadType: String?,
    val payloadValue: String?,
) {
    public companion object {
        public fun default(featureId: String, defaultValue: Boolean): FeatureFlag = FeatureFlag(
            userId = null,
            featureId = FeatureId(featureId),
            scope = Scope.Unknown,
            defaultValue = defaultValue,
            value = defaultValue,
            variantName = null,
            payloadType = null,
            payloadValue = null,
        )
    }
}

public enum class Scope(public val value: Int) {
    /* Requested but unknown. */
    Unknown(0),

    /* For this device. */
    Local(1),

    /* For this User. */
    User(2),

    /* For all Users. */
    Global(3),

    /* Source: Unleash */
    Unleash(4),
}
