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

import android.content.Context
import kotlinx.coroutines.runBlocking
import me.proton.core.challenge.data.frame.ChallengeFrame
import me.proton.core.challenge.domain.framePrefix
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.interceptor.CacheOverrideInterceptor
import me.proton.core.network.data.interceptor.DoHCookieInterceptor
import me.proton.core.network.data.interceptor.ServerErrorInterceptor
import me.proton.core.network.data.interceptor.ServerTimeInterceptor
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.RefreshTokenRequest
import me.proton.core.network.data.protonApi.RequestTokenRequest
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.session.ResolvedSession
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.domain.session.getResolvedSession
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Retrofit-based implementation of [ApiBackend] interface.
 *
 * @param Api Retrofit interface.
 * @property baseUrl Base url for the API.
 * @property client [ApiClient] to be used with the backend.
 * @property sessionId Optional [SessionId].
 * @property sessionProvider a [SessionProvider] to get the tokens from.
 * @property networkManager [NetworkManager] instance.
 * @param converters Retrofit converters to be used in the backend.
 * @param interfaceClass Kotlin class for [Api].
 * @param securityStrategy Strategy function introducing to okhttp builder pinning and other
 *   security features.
 */
@Suppress("LongParameterList")
internal class ProtonApiBackend<Api : BaseRetrofitApi>(
    private val context: Context,
    private val product: Product,
    override val baseUrl: String,
    private val client: ApiClient,
    private val clientIdProvider: ClientIdProvider,
    serverTimeListener: ServerTimeListener,
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val humanVerificationProvider: HumanVerificationProvider,
    private val deviceVerificationProvider: DeviceVerificationProvider,
    baseOkHttpClient: OkHttpClient,
    converters: List<Converter.Factory>,
    interfaceClass: KClass<Api>,
    private val networkManager: NetworkManager,
    securityStrategy: (OkHttpClient.Builder) -> Unit,
    private val networkPrefs: NetworkPrefs,
    private val cookieStore: ProtonCookieStore?,
    private val extraHeaderProvider: ExtraHeaderProvider? = null,
) : ApiBackend<Api> {

    private val api: Api

    private val okClient =
        baseOkHttpClient.newBuilder()
            .addInterceptor { orgChain ->
                val chain = handleTimeoutTag(orgChain)
                chain.proceed(prepareHeaders(chain.request()).build())
            }
            .addInterceptor(CacheOverrideInterceptor())
            .addInterceptor(ServerErrorInterceptor())
            .addNetworkInterceptor(ServerTimeInterceptor(serverTimeListener))
            .apply { cookieStore?.let { addInterceptor(DoHCookieInterceptor(networkPrefs, it)) } }
            .initLogging(client)
            .apply(securityStrategy)
            .build()

    init {
        val baseUrlFixed = if (!baseUrl.endsWith('/')) "$baseUrl/" else baseUrl
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrlFixed)
            .callFactory { okClient.newCall(it) }
        converters.forEach(retrofitBuilder::addConverterFactory)
        api = retrofitBuilder
            .build()
            .create(interfaceClass.java)
    }

    private fun handleTimeoutTag(chain: Interceptor.Chain): Interceptor.Chain {
        var chain = chain
        val tag = chain.request().tag(TimeoutOverride::class.java)
        tag?.let { timeout ->
            timeout.connectionTimeoutSeconds?.let {
                chain = chain.withConnectTimeout(it, TimeUnit.SECONDS)
            }
            timeout.readTimeoutSeconds?.let { chain = chain.withReadTimeout(it, TimeUnit.SECONDS) }
            timeout.writeTimeoutSeconds?.let {
                chain = chain.withWriteTimeout(it, TimeUnit.SECONDS)
            }
        }
        return chain
    }

    private fun prepareHeaders(original: Request): Request.Builder = runBlocking {
        val request = original.newBuilder()
            .header("x-pm-appversion", client.appVersionHeader)
            .header("x-pm-locale", Locale.getDefault().language)
            .header("User-Agent", client.userAgent)
            .method(original.method, original.body)

        // Set default Accept header if not present
        if (original.header("Accept") == null) {
            request.header("Accept", "application/vnd.protonmail.v1+json")
        }

        // Add session-related headers
        when (val resolved = sessionProvider.getResolvedSession(sessionId)) {
            is ResolvedSession.Found -> {
                resolved.session.sessionId.id.takeIfNotBlank()?.let { uid ->
                    request.header("x-pm-uid", uid)
                }
                resolved.session.accessToken.takeIfNotBlank()?.let { accessToken ->
                    request.header("Authorization", "Bearer $accessToken")
                }
            }
            is ResolvedSession.NotFound -> Unit
        }

        // Add human verification and device verification headers
        clientIdProvider.getClientId(sessionId)?.let { clientId ->
            humanVerificationProvider.getHumanVerificationDetails(clientId)?.let { details ->
                details.tokenType?.let { tokenType ->
                    request.header("x-pm-human-verification-token-type", tokenType)
                }
                details.tokenCode?.let { tokenCode ->
                    request.header("x-pm-human-verification-token", tokenCode)
                }
            }

            deviceVerificationProvider.getSolvedChallenge(sessionId)?.let { solvedChallenge ->
                request.header("x-pm-dv", solvedChallenge)
            }
        }

        // Add any additional headers
        extraHeaderProvider?.headers?.forEach {
            request.header(it.first, it.second)
        }

        return@runBlocking request
    }

    override suspend fun <T> invoke(call: ApiManager.Call<Api, T>): ApiResult<T> =
        invokeInternal(call.block)

    private suspend fun <T> invokeInternal(block: suspend Api.() -> T): ApiResult<T> =
        api.safeCall(networkManager, block)

    override suspend fun refreshSession(session: Session): ApiResult<Session> {
        CoreLogger.log(LogTag.REFRESH_TOKEN,
            "Refreshing tokens: uid=${session.sessionId.id.formatToken(client)}, refreshToken=${session.refreshToken.formatToken(client)}")
        val result = invokeInternal {
            refreshToken(
                RefreshTokenRequest(
                    uid = session.sessionId.id,
                    refreshToken = session.refreshToken
                )
            )
        }
        return when (result) {
            is ApiResult.Success -> {
                val accessToken = result.value.accessToken
                val refreshToken = result.value.refreshToken
                val tokensLog = "a=${accessToken.formatToken(client)} r=${refreshToken.formatToken(client)}"
                CoreLogger.log(LogTag.REFRESH_TOKEN, "Access & refresh tokens refreshed: $tokensLog.")
                ApiResult.Success(
                    session.refreshWith(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        scopes = result.value.scopes
                    )
                )
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun requestSession(): ApiResult<Session> {
        val name = "${product.framePrefix()}-0"
        val frame = ChallengeFrame.Device.build(context)
        val result = invokeInternal {
            requestToken(RequestTokenRequest(payload = mapOf(name to frame))).toSession()
        }
        return when (result) {
            is ApiResult.Success -> {
                CoreLogger.log(LogTag.REQUEST_TOKEN, "UnAuth tokens requested.")
                ApiResult.Success(
                    Session(
                        sessionId = result.value.sessionId,
                        accessToken = result.value.accessToken,
                        refreshToken = result.value.refreshToken,
                        scopes = result.value.scopes
                    )
                )
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun isPotentiallyBlocked(): Boolean =
        invokeInternal {
            ping(
                TimeoutOverride(
                    connectionTimeoutSeconds = client.pingTimeoutSeconds,
                    readTimeoutSeconds = client.pingTimeoutSeconds,
                    writeTimeoutSeconds = client.pingTimeoutSeconds
                )
            )
        }.isPotentialBlocking
}
