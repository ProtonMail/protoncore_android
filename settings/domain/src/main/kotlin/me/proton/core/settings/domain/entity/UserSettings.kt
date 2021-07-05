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

package me.proton.core.settings.domain.entity

data class UserSettings(
    val email: Setting?,
    val phone: Setting?,
    val password: Password,
    val twoFA: TwoFA?,
    val news: Int,
    val locale: String,
    val logAuth: Int,
    val invoiceText: String,
    val density: Int,
    val theme: String,
    val themeType: Int,
    val weekStart: Int,
    val dateFormat: Int,
    val timeFormat: Int,
    val welcome: Int,
    val earlyAccess: Int,
    val flags: Flags
)

data class Setting(
    val value: String,
    val status: Int,
    val notify: Int,
    val reset: Int
)

data class Password(
    val mode: Int,
    val expirationTime: Int?
)

data class TwoFA(
    val enabled: Int,
    val allowed: Int,
    val expirationTime: Int?,
    val u2fKeys: List<U2FKey>?
)

data class U2FKey(
    val label: String,
    val keyHandle: String,
    val compromised: Int
)

data class Flags(
    val welcomed: Int
)
