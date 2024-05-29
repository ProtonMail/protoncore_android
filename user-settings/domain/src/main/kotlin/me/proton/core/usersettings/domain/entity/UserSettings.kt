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

package me.proton.core.usersettings.domain.entity

import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum

data class UserSettings(
    val userId: UserId,
    val email: RecoverySetting?,
    val phone: RecoverySetting?,
    val password: PasswordSetting,
    val twoFA: TwoFASetting?,
    val news: Int?,
    val locale: String?,
    val logAuth: IntEnum<LogAuth>?,
    val density: IntEnum<Density>?,
    val weekStart: IntEnum<WeekStart>?,
    val dateFormat: IntEnum<DateFormat>?,
    val timeFormat: IntEnum<TimeFormat>?,
    val earlyAccess: Boolean?,
    val deviceRecovery: Boolean?,
    val telemetry: Boolean?,
    val crashReports: Boolean?,
    val sessionAccountRecovery: Boolean?
) {
    companion object {
        fun nil(userId: UserId): UserSettings = UserSettings(
            userId = userId,
            email = null,
            phone = null,
            password = PasswordSetting(null, null),
            twoFA = TwoFASetting.nil(),
            news = 0,
            locale = "en",
            logAuth = IntEnum(UserSettings.LogAuth.Disabled.value, UserSettings.LogAuth.Disabled),
            density = IntEnum(UserSettings.Density.Comfortable.value, UserSettings.Density.Comfortable),
            weekStart = IntEnum(UserSettings.WeekStart.Default.value, UserSettings.WeekStart.Default),
            dateFormat = IntEnum(UserSettings.DateFormat.Default.value, UserSettings.DateFormat.Default),
            timeFormat = IntEnum(UserSettings.TimeFormat.Default.value, UserSettings.TimeFormat.Default),
            earlyAccess = false,
            deviceRecovery = false,
            telemetry = false,
            crashReports = false,
            sessionAccountRecovery = false,
        )
    }

    enum class LogAuth(val value: Int) {
        Disabled(0),
        Basic(1),
        Advanced(2);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
        }
    }

    enum class Density(val value: Int) {
        Comfortable(0),
        Compact(1);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
        }
    }

    enum class WeekStart(val value: Int) {
        Default(0),
        Monday(1),
        Saturday(6),
        Sunday(7);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
        }
    }

    enum class DateFormat(val value: Int) {
        Default(0),
        DayMonthYear(1),
        MonthDayYear(2),
        YearMonthDay(3);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
        }
    }

    enum class TimeFormat(val value: Int) {
        Default(0),
        TwentyFourHours(1),
        TwelveHours(2);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
        }
    }
}

data class RecoverySetting(
    val value: String?,
    val status: Int?,
    val notify: Boolean?,
    val reset: Boolean?
)

data class PasswordSetting(
    val mode: Int?,
    val expirationTime: Int?
)

data class TwoFASetting(
    val enabled: Boolean?,
    val allowed: Int?,
    val expirationTime: Int?,
    val registeredKeys: List<Fido2RegisteredKey>
) {
    companion object {
        fun nil() = TwoFASetting(
            enabled = null,
            allowed = null,
            expirationTime = null,
            registeredKeys = emptyList()
        )
    }
}
