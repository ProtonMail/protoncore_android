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

package me.proton.core.network.domain

/**
 * Contains general constants response codes.
 */
object ResponseCodes {
    const val OK = 1000
    const val NOT_ALLOWED = 2011
    const val NOT_EXISTS = 2501
    const val APP_VERSION_BAD = 5003
    const val API_VERSION_INVALID = 5005
    const val PASSWORD_WRONG = 8002
    const val HUMAN_VERIFICATION_REQUIRED = 9001
    const val USER_CREATE_NAME_INVALID = 12_081
    const val USER_CREATE_TOKEN_INVALID = 12_087
    const val PAYMENTS_SUBSCRIPTION_NOT_EXISTS = 22_110

    val FORCE_UPDATE = listOf(APP_VERSION_BAD, API_VERSION_INVALID)
}
