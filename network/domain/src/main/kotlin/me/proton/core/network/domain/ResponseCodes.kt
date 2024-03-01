/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.network.domain

/**
 * Contains general constants response codes.
 */
object ResponseCodes {
    const val OK = 1000
    const val INVALID_REQUIREMENTS = 2000
    const val INVALID_VALUE = 2001
    const val VALUE_OUT_OF_BOUNDS = 2003
    const val NOT_SAME_AS_FIELD = 2010
    const val NOT_ALLOWED = 2011
    const val BANNED = 2028
    const val CURRENCY_FORMAT = 2053
    const val NOT_EXISTS = 2501
    const val APP_VERSION_BAD = 5003
    const val API_VERSION_INVALID = 5005
    const val APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS = 5099
    const val BODY_PARSE_FAILURE = 6001
    const val PASSWORD_WRONG = 8002
    const val AUTH_SWITCH_TO_SSO = 8100
    const val AUTH_SWITCH_TO_SRP = 8101
    const val HUMAN_VERIFICATION_REQUIRED = 9001
    const val DEVICE_VERIFICATION_REQUIRED = 9002 // new error code for device verification
    const val SCOPE_REAUTH_LOCKED = 9101
    const val SCOPE_REAUTH_PASSWORD = 9102
    const val ACCOUNT_FAILED_GENERIC = 10_001
    const val ACCOUNT_DELETED = 10_002
    const val ACCOUNT_DISABLED = 10_003
    const val ACCOUNT_CREDENTIALLESS_INVALID = 10_200
    const val USER_CREATE_NAME_INVALID = 12_081
    const val USER_CREATE_TOKEN_INVALID = 12_087
    const val USER_EXISTS_USERNAME_ALREADY_USED = 12_106
    const val PAYMENTS_SUBSCRIPTION_NOT_EXISTS = 22_110

    val FORCE_UPDATE = listOf(APP_VERSION_BAD, API_VERSION_INVALID)
}
