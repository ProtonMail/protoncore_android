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

package me.proton.core.auth.presentation.compose.sso

import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DurationFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public data class AuthDeviceData(
    val deviceId: AuthDeviceId,
    val name: String,
    val localizedClientName: String,
    val lastActivityTime: Long,
    val platform: AuthDevicePlatform,
    var lastActivityReadable: String? = null
)

public fun AuthDevice.toData(
    clock: Clock,
    durationFormat: DurationFormat
): AuthDeviceData {
    val now = clock.currentEpochSeconds()
    val delta = (now - lastActivityAtUtcSeconds).coerceAtLeast(60)
    return AuthDeviceData(
        deviceId = deviceId,
        name = name,
        localizedClientName = localizedClientName,
        lastActivityTime = lastActivityAtUtcSeconds,
        lastActivityReadable = durationFormat.format(
            duration = delta.toDuration(DurationUnit.SECONDS),
            startUnit = DurationUnit.HOURS,
            endUnit = DurationUnit.MINUTES
        ),
        platform = platform?.enum ?: AuthDevicePlatform.Android
    )
}
