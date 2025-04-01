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
import me.proton.core.auth.data.api.fido2.Fido2RegisteredKeyData

@Serializable
data class UserSettingsResponse(
    @SerialName("Email")
    val email: RecoverySettingResponse?,
    @SerialName("Phone")
    val phone: RecoverySettingResponse?,
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
    @SerialName("Density")
    val density: Int,
    @SerialName("WeekStart")
    val weekStart: Int,
    @SerialName("DateFormat")
    val dateFormat: Int,
    @SerialName("TimeFormat")
    val timeFormat: Int,
    @SerialName("EarlyAccess")
    val earlyAccess: Int,
    @SerialName("DeviceRecovery")
    val deviceRecovery: Int,
    @SerialName("Telemetry")
    val telemetry: Int,
    @SerialName("CrashReports")
    val crashReports: Int,
    @SerialName("SessionAccountRecovery")
    val sessionAccountRecovery: Int,
    @SerialName("Flags")
    val flags: FlagsResponse? = null
) {
    companion object {
        fun nil(): UserSettingsResponse = UserSettingsResponse(
            email = null,
            phone = null,
            password = PasswordResponse(0, null),
            twoFA = TwoFAResponse(0, 0, null, null),
            news = 0,
            locale = "en",
            logAuth = 0,
            density = 0,
            weekStart = 0,
            dateFormat = 0,
            timeFormat = 0,
            earlyAccess = 0,
            deviceRecovery = 0,
            telemetry = 0,
            crashReports = 0,
            sessionAccountRecovery = 0,
        )
    }
}

@Serializable
data class RecoverySettingResponse(
    @SerialName("Value")
    val value: String?,
    @SerialName("Status")
    val status: Int,
    @SerialName("Notify")
    val notify: Int,
    @SerialName("Reset")
    val reset: Int
)

@Serializable
data class PasswordResponse(
    @SerialName("Mode")
    val mode: Int,
    @SerialName("ExpirationTime")
    val expirationTime: Int?
)

@Serializable
data class TwoFAResponse(
    @SerialName("Enabled")
    val enabled: Int,
    @SerialName("Allowed")
    val allowed: Int,
    @SerialName("ExpirationTime")
    val expirationTime: Int?,
    @SerialName("RegisteredKeys")
    val registeredKeys: List<Fido2RegisteredKeyData>?
)

@Serializable
data class FlagsResponse(
    @SerialName("EdmOptOut")
    val easyDeviceMigrationOptOut: Int? = null
)
