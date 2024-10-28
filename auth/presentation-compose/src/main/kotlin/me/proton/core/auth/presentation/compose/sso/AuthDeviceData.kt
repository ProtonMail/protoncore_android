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

import android.content.Context
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import me.proton.core.auth.presentation.compose.R
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DurationFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
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
    context: Context,
    clock: Clock,
    durationFormat: DurationFormat,
    dateTimeFormat: DateTimeFormat
): AuthDeviceData {
    val now = clock.currentEpochSeconds()
    val delta = (now - lastActivityAtUtcSeconds).coerceAtLeast(60)
    val duration = delta.toDuration(DurationUnit.SECONDS)
    return AuthDeviceData(
        deviceId = deviceId,
        name = name,
        localizedClientName = localizedClientName,
        lastActivityTime = lastActivityAtUtcSeconds,
        lastActivityReadable = when {
            duration > 24.hours -> dateTimeFormat.formatDate(context, lastActivityAtUtcSeconds)
            else -> durationFormat.formatDuration(context, duration)
        },
        platform = platform?.enum ?: AuthDevicePlatform.Android
    )
}

private fun DateTimeFormat.formatDate(
    context: Context,
    epochSeconds: Long
): String = context.getString(
    R.string.auth_login_device_last_used_on_date,
    format(
        epochSeconds = epochSeconds,
        style = DateTimeFormat.DateTimeForm.MEDIUM_DATE
    )
)

private fun DurationFormat.formatDuration(
    context: Context,
    duration: Duration
): String = context.getString(
    R.string.auth_login_device_last_used_duration_ago,
    format(
        duration = duration,
        startUnit = DurationUnit.HOURS,
        endUnit = DurationUnit.MINUTES
    )
)
