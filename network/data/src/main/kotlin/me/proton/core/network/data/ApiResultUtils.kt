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

import kotlinx.serialization.SerializationException
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Wrap the result with [ApiResult], catching expected Network related exceptions.
 */
suspend fun <Api: BaseRetrofitApi, T> Api.safeCall(
    networkManager: NetworkManager,
    block: suspend Api.() -> T
): ApiResult<T> {
    val result = runCatching {
        ApiResult.Success(block(this))
    }.getOrElse { e ->
        when (e) {
            is ProtonErrorException -> parseHttpError(e.response, e.protonData, e)
            is HttpException -> parseHttpError(e.response()!!.raw(), null, e)
            is SerializationException -> ApiResult.Error.Parse(e)
            is CertificateException -> ApiResult.Error.Certificate(e)
            is SSLHandshakeException -> ApiResult.Error.Certificate(e)
            is SSLPeerUnverifiedException -> ApiResult.Error.Certificate(e)
            is SocketTimeoutException -> ApiResult.Error.Timeout(networkManager.isConnectedToNetwork(), e)
            is UnknownHostException -> ApiResult.Error.Connection(networkManager.isConnectedToNetwork(), e)
            is IOException -> ApiResult.Error.Connection(networkManager.isConnectedToNetwork(), e)
            else -> throw e // Throw any other unexpected exception.
        }
    }
    if (result is ApiResult.Error) {
        CoreLogger.log(LogTag.API_ERROR, result.toString())
    }
    return result
}

private fun <T> parseHttpError(
    response: Response,
    protonData: ApiResult.Error.ProtonData?,
    cause: Exception
): ApiResult<T> {
    return ApiResult.Error.Http(response.code, response.message, protonData, cause, response.headers.retryAfter())
}

data class ProtonErrorException(
    val response: Response,
    val protonData: ApiResult.Error.ProtonData
) : IOException()
