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
import me.proton.core.usersettings.data.api.response.FlagsResponse
import me.proton.core.usersettings.data.api.response.PasswordResponse
import me.proton.core.usersettings.data.api.response.RecoverySettingResponse
import me.proton.core.usersettings.data.api.response.TwoFAResponse
import me.proton.core.usersettings.data.api.response.U2FKeyResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.data.entity.FlagsEntity
import me.proton.core.usersettings.data.entity.PasswordEntity
import me.proton.core.usersettings.data.entity.RecoverySettingEntity
import me.proton.core.usersettings.data.entity.TwoFAEntity
import me.proton.core.usersettings.data.entity.U2FKeyEntity
import me.proton.core.usersettings.data.entity.UserSettingsEntity
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.TwoFASetting
import me.proton.core.usersettings.domain.entity.U2FKeySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.util.kotlin.toBoolean
import me.proton.core.util.kotlin.toInt

internal fun UserSettingsResponse.fromResponse(userId: UserId) = UserSettings(
    userId = userId,
    email = email?.fromResponse(),
    phone = phone?.fromResponse(),
    password = password.fromResponse(),
    twoFA = twoFA?.fromResponse(),
    news = news,
    locale = locale,
    logAuth = logAuth,
    invoiceText = invoiceText,
    density = density,
    theme = theme,
    themeType = themeType,
    weekStart = weekStart,
    dateFormat = dateFormat,
    timeFormat = timeFormat,
    welcome = welcome.toBoolean(),
    earlyAccess = earlyAccess.toBoolean(),
    flags = flags?.fromResponse()
)

internal fun RecoverySettingResponse.fromResponse() = RecoverySetting(value, status, notify.toBoolean(), reset.toBoolean())

internal fun PasswordResponse.fromResponse() = PasswordSetting(mode, expirationTime)

internal fun TwoFAResponse.fromResponse() = TwoFASetting(enabled.toBoolean(), allowed, expirationTime, u2fKeys?.map {
    it.fromResponse()
})

internal fun U2FKeyResponse.fromResponse() = U2FKeySetting(label, keyHandle, compromised.toBoolean())

internal fun FlagsResponse.fromResponse() = Flags(welcomed.toBoolean())

internal fun UserSettingsEntity.fromEntity() = UserSettings(
    userId = userId,
    email = email?.fromEntity(),
    phone = phone?.fromEntity(),
    password = password.fromEntity(),
    twoFA = twoFA?.fromEntity(),
    news = news,
    locale = locale,
    logAuth = logAuth,
    invoiceText = invoiceText,
    density = density,
    theme = theme,
    themeType = themeType,
    weekStart = weekStart,
    dateFormat = dateFormat,
    timeFormat = timeFormat,
    welcome = welcome,
    earlyAccess = earlyAccess,
    flags = flags?.fromEntity()
)

internal fun UserSettings.toEntity() = UserSettingsEntity(
    userId = userId,
    email = email?.toEntity(),
    phone = phone?.toEntity(),
    password = password.toEntity(),
    twoFA = twoFA?.toEntity(),
    news = news,
    locale = locale,
    logAuth = logAuth,
    invoiceText = invoiceText,
    density = density,
    theme = theme,
    themeType = themeType,
    weekStart = weekStart,
    dateFormat = dateFormat,
    timeFormat = timeFormat,
    welcome = welcome,
    earlyAccess = earlyAccess,
    flags = flags?.toEntity()
)

internal fun RecoverySettingEntity.fromEntity() = RecoverySetting(
    value = value,
    status = status,
    notify = notify.toBoolean(),
    reset = reset.toBoolean()
)

internal fun RecoverySetting.toEntity() = RecoverySettingEntity(
    value = value,
    status = status,
    notify = notify.toInt(),
    reset = reset.toInt()
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
    enabled = enabled.toBoolean(),
    allowed = allowed,
    expirationTime = expirationTime,
    u2fKeys = u2fKeys?.map {
        it.fromEntity()
    }
)

internal fun TwoFASetting.toEntity() = TwoFAEntity(
    enabled = enabled.toInt(),
    allowed = allowed.toInt(),
    expirationTime = expirationTime,
    u2fKeys = u2fKeys?.map {
        it.toEntity()
    }
)

internal fun U2FKeyEntity.fromEntity() = U2FKeySetting(
    label = label,
    keyHandle = keyHandle,
    compromised = compromised.toBoolean()
)

internal fun U2FKeySetting.toEntity() = U2FKeyEntity(
    label = label,
    keyHandle = keyHandle,
    compromised = compromised.toInt()
)

internal fun FlagsEntity.fromEntity() = Flags(
    welcomed = welcomed
)

internal fun Flags.toEntity() = FlagsEntity(
    welcomed = welcomed
)
