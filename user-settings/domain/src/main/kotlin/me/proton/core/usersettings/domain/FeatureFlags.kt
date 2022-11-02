/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.usersettings.domain

import me.proton.core.featureflag.domain.entity.FeatureId

/**
 * This class contains all the feature flags that are used by [user-settings] modules.
 */
enum class FeatureFlags(val id: FeatureId, val default: Boolean) {
    // Remote flags
    ShowDataCollectSettings(id = FeatureId("ShowDataCollectSettings"), default = false),
}
