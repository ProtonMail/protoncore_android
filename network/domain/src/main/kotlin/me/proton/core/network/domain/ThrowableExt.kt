/*
 * Copyright (c) 2024 Proton AG
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

import me.proton.core.network.domain.ResponseCodes.ACCOUNT_CREDENTIALLESS_INVALID
import me.proton.core.network.domain.ResponseCodes.APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS
import me.proton.core.network.domain.ResponseCodes.AUTH_SWITCH_TO_SRP
import me.proton.core.network.domain.ResponseCodes.AUTH_SWITCH_TO_SSO
import me.proton.core.network.domain.ResponseCodes.NOT_ALLOWED
import me.proton.core.network.domain.ResponseCodes.NOT_EXISTS
import me.proton.core.network.domain.ResponseCodes.PASSWORD_WRONG
import me.proton.core.network.domain.ResponseCodes.SCOPE_REAUTH_LOCKED
import me.proton.core.network.domain.ResponseCodes.SCOPE_REAUTH_PASSWORD

fun Throwable.isApiProtonError(vararg code: Int) = when (this) {
    is ApiException -> hasProtonErrorCode(*code)
    else -> false
}

fun Throwable.isActionNotAllowed() = isApiProtonError(NOT_ALLOWED)
fun Throwable.isNotExists() = isApiProtonError(NOT_EXISTS)
fun Throwable.isCredentialLessDisabled() = isApiProtonError(ACCOUNT_CREDENTIALLESS_INVALID)
fun Throwable.isExternalNotSupported() = isApiProtonError(APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS)
fun Throwable.isMissingScope() = isApiProtonError(SCOPE_REAUTH_LOCKED, SCOPE_REAUTH_PASSWORD)
fun Throwable.isWrongPassword() = isApiProtonError(PASSWORD_WRONG)
fun Throwable.isSwitchToSrp() = isApiProtonError(AUTH_SWITCH_TO_SRP)
fun Throwable.isSwitchToSso() = isApiProtonError(AUTH_SWITCH_TO_SSO)
