/*
 * Copyright (c) 2020 Proton Technologies AG
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
package me.proton.core.network.data

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.exception.ApiConnectionException
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal suspend fun <Api, T> safeApiCall(
    networkManager: NetworkManager,
    api: Api,
    block: suspend (Api) -> T
): ApiResult<T> {
    val result = runCatching {
        ApiResult.Success(block(api))
    }.getOrElse { e ->
        when (e) {
            is CancellationException -> throw e
            is ProtonErrorException -> parseHttpError(e.response, e.protonData, e)
            is HttpException -> parseHttpError(e.response()!!.raw(), null, e)
            is SerializationException -> ApiResult.Error.Parse(e)
            is CertificateException -> ApiResult.Error.Certificate(e)
            is ApiConnectionException -> e.toApiResult(networkManager)
            else -> ApiResult.Error.Connection(networkManager.isConnectedToNetwork(), e)
        }
    }
    if (result is ApiResult.Error) {
        CoreLogger.log(LogTag.API_ERROR, result.toString())
    }
    return result
}

private fun ApiConnectionException.toApiResult(networkManager: NetworkManager): ApiResult.Error.Connection {
    // handle the exceptions that might indicate that the API is potentially blocked
    return when (originalException) {
        is SSLHandshakeException -> ApiResult.Error.Certificate(this)
        is SSLPeerUnverifiedException -> ApiResult.Error.Certificate(this)
        is SocketTimeoutException -> ApiResult.Error.Timeout(networkManager.isConnectedToNetwork(), this)
        is UnknownHostException -> ApiResult.Error.Connection(networkManager.isConnectedToNetwork(), this)
        else -> ApiResult.Error.Connection(networkManager.isConnectedToNetwork(), this)
    }
}

private fun <T> parseHttpError(
    response: Response,
    protonData: ApiResult.Error.ProtonData?,
    cause: Exception
): ApiResult<T> {
    return if (response.code == ApiResult.HTTP_TOO_MANY_REQUESTS) {
        val retryAfter = response.headers["Retry-After"]?.toIntOrNull() ?: 0
        ApiResult.Error.TooManyRequest(retryAfter, protonData)
    } else {
        ApiResult.Error.Http(response.code, response.message, protonData, cause)
    }
}

data class ProtonErrorException(
    val response: Response,
    val protonData: ApiResult.Error.ProtonData
) : IOException()
