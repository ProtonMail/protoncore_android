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

package me.proton.core.usersettings.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.Password
import me.proton.core.usersettings.domain.entity.Setting
import me.proton.core.usersettings.domain.entity.TwoFA
import me.proton.core.usersettings.domain.entity.U2FKey
import me.proton.core.usersettings.domain.entity.UserSettings

@Serializable
data class UserSettingsResponse(
    @SerialName("Email")
    val email: SettingResponse?,
    @SerialName("Phone")
    val phone: SettingResponse?,
    @SerialName("Password")
    val password: PasswordResponse,
    @SerialName("2FA")
    val twoFA: TwoFAResponse?,
    @SerialName("News")
    val news: Int,
    @SerialName("Locale")
    val locale: String,
    @SerialName("LogAuth")
    val logAuth: Int,
    @SerialName("InvoiceText")
    val invoiceText: String,
    @SerialName("Density")
    val density: Int,
    @SerialName("Theme")
    val theme: String?,
    @SerialName("ThemeType")
    val themeType: Int,
    @SerialName("WeekStart")
    val weekStart: Int,
    @SerialName("DateFormat")
    val dateFormat: Int,
    @SerialName("TimeFormat")
    val timeFormat: Int,
    @SerialName("Welcome")
    val welcome: Int,
    @SerialName("EarlyAccess")
    val earlyAccess: Int,
    @SerialName("Flags")
    val flags: FlagsResponse?
) {
    fun toUserSettings(): UserSettings =
        UserSettings(
            email = email?.toSetting(),
            phone = phone?.toSetting(),
            password = password.toPassword(),
            twoFA = twoFA?.toTwoFA(),
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
            flags = flags?.toFlags()
        )
}

@Serializable
data class SettingResponse(
    @SerialName("Value")
    val value: String?,
    @SerialName("Status")
    val status: Int,
    @SerialName("Notify")
    val notify: Int,
    @SerialName("Reset")
    val reset: Int
) {
    fun toSetting(): Setting = Setting(value, status, notify, reset)
}

@Serializable
data class PasswordResponse(
    @SerialName("Mode")
    val mode: Int,
    @SerialName("ExpirationTime")
    val expirationTime: Int?
) {
    fun toPassword(): Password = Password(mode, expirationTime)
}

@Serializable
data class TwoFAResponse(
    @SerialName("Enabled")
    val enabled: Int,
    @SerialName("Allowed")
    val allowed: Int,
    @SerialName("ExpirationTime")
    val expirationTime: Int?,
    @SerialName("U2FKeys")
    val u2fKeys: List<U2FKeyResponse>?
) {
    fun toTwoFA(): TwoFA = TwoFA(enabled, allowed, expirationTime, u2fKeys?.map {
        it.toU2FKey()
    })
}

@Serializable
data class U2FKeyResponse(
    @SerialName("Label")
    val label: String,
    @SerialName("KeyHandle")
    val keyHandle: String,
    @SerialName("Compromised")
    val compromised: Int
) {
    fun toU2FKey(): U2FKey = U2FKey(label, keyHandle, compromised)
}

@Serializable
data class FlagsResponse(
    @SerialName("Welcomed")
    val welcomed: Int
) {
    fun toFlags(): Flags = Flags(welcomed)
}
