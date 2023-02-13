/*
 * Copyright (c) 2022 Proton Technologies AG
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

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import javax.net.ssl.SSLException

@Suppress("EnumNaming", "EnumEntryName")
public enum class HttpApiStatus {
    http2xx,
    http4xx,
    http5xx,
    connectionError,
    parseError,
    sslError,
    unknown
}

public fun <R> Result<R>.toHttpApiStatus(): HttpApiStatus =
    exceptionOrNull()?.toHttpApiStatus() ?: HttpApiStatus.http2xx

@Suppress("MagicNumber")
public fun Throwable.toHttpApiStatus(): HttpApiStatus = when (this) {
    is ApiException -> when (val apiError = this.error) {
        is ApiResult.Error.Certificate -> HttpApiStatus.sslError
        is ApiResult.Error.Connection -> HttpApiStatus.connectionError
        is ApiResult.Error.Http -> when (apiError.httpCode) {
            in 400..499 -> HttpApiStatus.http4xx
            in 500..599 -> HttpApiStatus.http5xx
            else -> HttpApiStatus.unknown
        }
        is ApiResult.Error.Parse -> HttpApiStatus.parseError
    }
    is SSLException -> HttpApiStatus.sslError
    else -> HttpApiStatus.unknown
}
