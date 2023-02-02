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

package me.proton.core.observability.data

import android.content.Context
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.usersettings.domain.entity.DeviceSettings
import javax.inject.Inject

public class IsObservabilityEnabledImpl @Inject constructor(
    private val context: Context,
    private val deviceSettings: DeviceSettings
) : IsObservabilityEnabled {

    override suspend fun invoke(): Boolean =
        deviceSettings.isTelemetryEnabled && context.resources.getBoolean(R.bool.observability_enabled)
}