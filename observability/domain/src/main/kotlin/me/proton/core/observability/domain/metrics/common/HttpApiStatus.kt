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

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger
import java.text.ParseException
import javax.net.ssl.SSLException

@Suppress("EnumNaming", "EnumEntryName")
public enum class HttpApiStatus {
    http1xx,
    http2xx,
    http3xx,
    http4xx,
    http5xx,
    connectionError,
    notConnected,
    parseError,
    sslError,
    cancellation,
    unknown
}

public fun <R> Result<R>.toHttpApiStatus(): HttpApiStatus =
    exceptionOrNull()?.toHttpApiStatus() ?: HttpApiStatus.http2xx

@Suppress("MagicNumber", "CyclomaticComplexMethod")
public fun Throwable.toHttpApiStatus(): HttpApiStatus = when (this) {
    is ApiException -> when (val apiError = this.error) {
        is ApiResult.Error.Certificate -> HttpApiStatus.sslError
        is ApiResult.Error.Connection -> when (apiError.isConnectedToNetwork) {
            true -> HttpApiStatus.connectionError
            false -> HttpApiStatus.notConnected
        }
        is ApiResult.Error.Http -> when (apiError.httpCode) {
            in 100..199 -> HttpApiStatus.http1xx
            in 200..299 -> HttpApiStatus.http2xx
            in 300..399 -> HttpApiStatus.http3xx
            in 400..499 -> HttpApiStatus.http4xx
            in 500..599 -> HttpApiStatus.http5xx
            else -> HttpApiStatus.unknown.also { CoreLogger.e(LogTag.UNKNOWN, this) }
        }
        is ApiResult.Error.Parse -> HttpApiStatus.parseError.also { CoreLogger.e(LogTag.PARSE, this) }
    }
    is SSLException -> HttpApiStatus.sslError
    is CancellationException -> HttpApiStatus.cancellation
    is SerializationException -> HttpApiStatus.parseError.also { CoreLogger.e(LogTag.PARSE, this) }
    is ParseException -> HttpApiStatus.parseError.also { CoreLogger.e(LogTag.PARSE, this) }
    else -> HttpApiStatus.unknown.also { CoreLogger.e(LogTag.UNKNOWN, this) }
}
