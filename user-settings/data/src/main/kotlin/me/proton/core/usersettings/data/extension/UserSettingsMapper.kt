/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.usersettings.data.extension

import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.data.api.response.PasswordResponse
import me.proton.core.usersettings.data.api.response.RecoverySettingResponse
import me.proton.core.usersettings.data.api.response.TwoFAResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.data.entity.PasswordEntity
import me.proton.core.usersettings.data.entity.RecoverySettingEntity
import me.proton.core.usersettings.data.entity.TwoFAEntity
import me.proton.core.usersettings.data.entity.UserSettingsEntity
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.TwoFASetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.entity.UserSettings.DateFormat
import me.proton.core.usersettings.domain.entity.UserSettings.Density
import me.proton.core.usersettings.domain.entity.UserSettings.LogAuth
import me.proton.core.usersettings.domain.entity.UserSettings.TimeFormat
import me.proton.core.usersettings.domain.entity.UserSettings.WeekStart
import me.proton.core.util.kotlin.toBooleanOrFalse
import me.proton.core.util.kotlin.toBooleanOrTrue
import me.proton.core.util.kotlin.toInt

fun UserSettingsResponse.toUserSettings(userId: UserId): UserSettings = fromResponse(userId)

internal fun UserSettingsResponse.fromResponse(userId: UserId) = UserSettings(
    userId = userId,
    email = email?.fromResponse(),
    phone = phone?.fromResponse(),
    password = password.fromResponse(),
    twoFA = twoFA?.fromResponse(),
    news = news,
    locale = locale,
    logAuth = LogAuth.enumOf(logAuth),
    density = Density.enumOf(density),
    weekStart = WeekStart.enumOf(weekStart),
    dateFormat = DateFormat.enumOf(dateFormat),
    timeFormat = TimeFormat.enumOf(timeFormat),
    earlyAccess = earlyAccess.toBooleanOrFalse(),
    telemetry = telemetry.toBooleanOrFalse(),
)

internal fun RecoverySettingResponse.fromResponse() = RecoverySetting(
    value = value,
    status = status,
    notify = notify.toBooleanOrFalse(),
    reset = reset.toBooleanOrFalse()
)

internal fun PasswordResponse.fromResponse() = PasswordSetting(
    mode = mode,
    expirationTime = expirationTime
)

internal fun TwoFAResponse.fromResponse() = TwoFASetting(
    enabled = enabled.toBooleanOrTrue(),
    allowed = allowed,
    expirationTime = expirationTime,
)

internal fun UserSettingsEntity.fromEntity() = UserSettings(
    userId = userId,
    email = email?.fromEntity(),
    phone = phone?.fromEntity(),
    password = password.fromEntity(),
    twoFA = twoFA?.fromEntity(),
    news = news,
    locale = locale,
    logAuth = LogAuth.enumOf(logAuth),
    density = Density.enumOf(density),
    weekStart = WeekStart.enumOf(weekStart),
    dateFormat = DateFormat.enumOf(dateFormat),
    timeFormat = TimeFormat.enumOf(timeFormat),
    earlyAccess = earlyAccess,
    telemetry = telemetry,
)

internal fun UserSettings.toEntity() = UserSettingsEntity(
    userId = userId,
    email = email?.toEntity(),
    phone = phone?.toEntity(),
    password = password.toEntity(),
    twoFA = twoFA?.toEntity(),
    news = news,
    locale = locale,
    logAuth = logAuth?.value,
    density = density?.value,
    weekStart = weekStart?.value,
    dateFormat = dateFormat?.value,
    timeFormat = timeFormat?.value,
    earlyAccess = earlyAccess,
    telemetry = telemetry,
)

internal fun RecoverySettingEntity.fromEntity() = RecoverySetting(
    value = value,
    status = status,
    notify = notify?.toBooleanOrFalse(),
    reset = reset?.toBooleanOrFalse()
)

internal fun RecoverySetting.toEntity() = RecoverySettingEntity(
    value = value,
    status = status,
    notify = notify?.toInt(),
    reset = reset?.toInt()
)

internal fun PasswordEntity.fromEntity() = PasswordSetting(
    mode = mode,
    expirationTime = expirationTime
)

internal fun PasswordSetting.toEntity() = PasswordEntity(
    mode = mode,
    expirationTime = expirationTime
)

internal fun TwoFAEntity.fromEntity() = TwoFASetting(
    enabled = enabled?.toBooleanOrTrue(),
    allowed = allowed,
    expirationTime = expirationTime,
)

internal fun TwoFASetting.toEntity() = TwoFAEntity(
    enabled = enabled?.toInt(),
    allowed = allowed,
    expirationTime = expirationTime,
)
