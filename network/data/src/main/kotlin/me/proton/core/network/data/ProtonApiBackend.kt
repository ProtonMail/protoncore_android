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

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.ProtonErrorData
import me.proton.core.network.data.protonApi.RefreshTokenRequest
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.UserData
import me.proton.core.util.kotlin.deserializeOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Retrofit-based implementation of [ApiBackend] interface.
 *
 * @param Api Retrofit interface.
 * @property baseUrl Base url for the API.
 * @property client [ApiClient] to be used with the backend.
 * @property userData [UserData] bound to this backend.
 * @property networkManager [NetworkManager] instance.
 * @param converters Retrofit converters to be used in the backend.
 * @param interfaceClass Kotlin class for [Api].
 * @param securityStrategy Strategy function introducing to okhttp builder pinning and other
 *   security features.
 */
internal class ProtonApiBackend<Api : BaseRetrofitApi>(
    override val baseUrl: String,
    private val client: ApiClient,
    private val userData: UserData,
    baseOkHttpClient: OkHttpClient,
    converters: List<Converter.Factory>,
    interfaceClass: KClass<Api>,
    private val networkManager: NetworkManager,
    securityStrategy: (OkHttpClient.Builder) -> Unit
) : ApiBackend<Api> {

    private val api: Api

    init {
        val builder = baseOkHttpClient.newBuilder()
            .addInterceptor { orgChain ->
                val chain = handleTimeoutTag(orgChain)
                chain.proceed(prepareHeaders(chain.request()).build())
            }.addInterceptor { chain ->
                interceptErrors(chain)
            }
        securityStrategy(builder)

        val baseUrlFixed = if (!baseUrl.endsWith('/'))
            "$baseUrl/" else baseUrl
        val okClient = builder.build()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrlFixed)
            .client(okClient)
        converters.forEach(retrofitBuilder::addConverterFactory)
        api = retrofitBuilder
            .build()
            .create(interfaceClass.java)
    }

    private fun handleTimeoutTag(chain: Interceptor.Chain): Interceptor.Chain {
        val tag = chain.request().tag()
        return if (tag is TimeoutOverride) {
            chain
                .withConnectTimeout(tag.connectionTimeoutSeconds, TimeUnit.SECONDS)
                .withReadTimeout(tag.readTimeoutSeconds, TimeUnit.SECONDS)
                .withWriteTimeout(tag.writeTimeoutSeconds, TimeUnit.SECONDS)
        } else {
            chain
        }
    }

    private fun prepareHeaders(original: Request): Request.Builder {
        val request = original.newBuilder()
            .header("x-pm-appversion", client.appVersionHeader)
            .header("x-pm-apiversion", "3")
            .header("x-pm-locale", Locale.getDefault().language)
            .header("User-Agent", client.userAgent)
            .method(original.method, original.body)
        if (original.header("Accept") == null) {
            request.header("Accept", "application/vnd.protonmail.v1+json")
        }
        val uid = userData.sessionUid
        val accessToken = userData.accessToken
        if (uid.isNotEmpty())
            request.addHeader("x-pm-uid", uid)
        if (accessToken.isNotEmpty())
            request.addHeader("Authorization", accessToken)
        return request
    }

    private fun interceptErrors(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            val errorBody = response.peekBody(MAX_ERROR_BYTES).string()
            val protonError = errorBody.deserializeOrNull(ProtonErrorData.serializer())?.apiResultData
            if (protonError != null)
                throw ProtonErrorException(response, protonError)
        }

        return response
    }

    override suspend fun <T> invoke(call: ApiManager.Call<Api, T>): ApiResult<T> =
        invokeInternal(call.block)

    private suspend fun <T> invokeInternal(block: suspend Api.() -> T): ApiResult<T> =
        safeApiCall(networkManager, api, block)

    override suspend fun refreshTokens(): ApiResult<ApiBackend.Tokens> {
        val result = invokeInternal {
            refreshToken(RefreshTokenRequest(
                refreshToken = userData.refreshToken, uid = userData.sessionUid))
        }
        return when (result) {
            is ApiResult.Success -> ApiResult.Success(ApiBackend.Tokens(
                access = result.value.accessToken,
                refresh = result.value.refreshToken))
            is ApiResult.Error -> result
        }
    }

    override suspend fun isPotentiallyBlocked(): Boolean =
        invokeInternal { ping() }.isPotentialBlocking

    companion object {
        private const val MAX_ERROR_BYTES = 1_000_000L
    }
}
