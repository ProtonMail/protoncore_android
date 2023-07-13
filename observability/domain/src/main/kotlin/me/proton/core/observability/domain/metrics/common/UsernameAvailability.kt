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

package me.proton.core.observability.domain.metrics.common

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.hasProtonErrorCode

@Serializable
public data class UsernameAvailabilityLabels constructor(
    @get:Schema(required = true)
    val status: UsernameAvailabilityStatus
)

@Suppress("EnumNaming", "EnumEntryName")
public enum class UsernameAvailabilityStatus {
    http2xx,
    http409UsernameConflict,
    http409,
    http422,
    http4xx,
    http5xx,
    connectionError,
    notConnected,
    parseError,
    sslError,
    unknown
}

internal fun Result<*>.toUsernameAvailabilityStatus(): UsernameAvailabilityStatus = when {
    isUsernameConflict() -> UsernameAvailabilityStatus.http409UsernameConflict
    isHttpError(HttpResponseCodes.HTTP_CONFLICT) -> UsernameAvailabilityStatus.http409
    isHttpError(HttpResponseCodes.HTTP_UNPROCESSABLE) -> UsernameAvailabilityStatus.http422
    else -> toHttpApiStatus().toUsernameAvailabilityStatus()
}

private fun Result<*>.isUsernameConflict(): Boolean =
    isHttpError(HttpResponseCodes.HTTP_CONFLICT) &&
            exceptionOrNull()?.hasProtonErrorCode(ResponseCodes.USER_EXISTS_USERNAME_ALREADY_USED) == true

private fun HttpApiStatus.toUsernameAvailabilityStatus(): UsernameAvailabilityStatus =
    when (this) {
        HttpApiStatus.http2xx -> UsernameAvailabilityStatus.http2xx
        HttpApiStatus.http4xx -> UsernameAvailabilityStatus.http4xx
        HttpApiStatus.http5xx -> UsernameAvailabilityStatus.http5xx
        HttpApiStatus.connectionError -> UsernameAvailabilityStatus.connectionError
        HttpApiStatus.notConnected -> UsernameAvailabilityStatus.notConnected
        HttpApiStatus.parseError -> UsernameAvailabilityStatus.parseError
        HttpApiStatus.sslError -> UsernameAvailabilityStatus.sslError
        HttpApiStatus.unknown -> UsernameAvailabilityStatus.unknown
    }
