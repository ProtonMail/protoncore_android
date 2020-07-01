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

package me.proton.android.core.data.api.interceptor

import me.proton.android.core.data.api.AUTH_INFO_PATH
import me.proton.android.core.data.api.AUTH_PATH
import me.proton.android.core.data.api.REFRESH_PATH
import me.proton.android.core.data.api.RESPONSE_CODE_TOO_MANY_REQUESTS
import me.proton.android.core.data.api.service.ProtonPublicService
import me.proton.android.core.data.api.entity.AuthTag
import me.proton.android.core.data.api.manager.TokenManager
import me.proton.android.core.data.api.manager.UserManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

/**
 * Created by dinokadrikj on 4/6/20.
 */

const val RESPONSE_CODE_UNAUTHORIZED = 401
const val API_VERSION = "3"
const val HEADER_LOCALE = "x-pm-locale"
const val HEADER_UID = "x-pm-uid"
const val HEADER_API_VERSION = "x-pm-apiversion"
const val HEADER_APP_VERSION = "x-pm-appversion"
const val HEADER_AUTH = "Authorization"
const val HEADER_USER_AGENT = "User-Agent"

/**
 *
 */
abstract class BaseProtonApiInterceptor(
    private val appVersion: String,
    private val userAgent: String,
    private val locale: String,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val api: ProtonPublicService
) : Interceptor {

    /**
     * Checks the auth token expiration.
     */
    fun checkExpiration(chain: Interceptor.Chain, request: Request, response: Response?): Response? {
        if (response == null) {
            return null
        }

        if (response.code() == RESPONSE_CODE_UNAUTHORIZED) {
            val usernameAuth = chain.request().tag(AuthTag::class.java)?.username ?: userManager.username
            val tokenManager = userManager.getTokenManager(usernameAuth)

            if (tokenManager != null && !request.url().encodedPath().contains(REFRESH_PATH)) {
                synchronized(this) {
                    Timber.d("access token expired, trying to refresh")
                    // get a new token with synchronous retrofit call
                    val refreshBody = tokenManager.createRefreshBody()

                    val refreshResponse = api.refreshSync(refreshBody,
                        AuthTag(
                            usernameAuth
                        )
                    ).execute()
                    val refreshResponseBody = refreshResponse.body()
                    if (refreshResponseBody?.accessToken != null) {
                        Timber.tag("429")
                                .i("access token expired, got correct refresh response, handle refresh in token manager")
                        tokenManager.handleRefresh(refreshResponseBody)
                    } else {
                        return if (refreshResponse.code() == RESPONSE_CODE_TOO_MANY_REQUESTS) {
                            Timber.tag("429")
                                .i("access token expired, got 429 response trying to refresh it, quitting flow")
                            null
                        } else {
                            userManager.logout(false, usernameAuth)
                            response
                        }
                    }

                    // update request with new token
                    val newRequest: Request
                    if (tokenManager.authAccessToken != null) {
                        Timber.d("access token expired, updating request with new token")
                        newRequest = request.newBuilder()
                                .header(HEADER_AUTH, tokenManager.authAccessToken!!)
                                .header(HEADER_UID, tokenManager.userID)
                                .header(
                                    HEADER_API_VERSION,
                                    API_VERSION
                                )
                                .header(HEADER_APP_VERSION, appVersion)
                                .header(HEADER_USER_AGENT, userAgent)
                                .header(HEADER_LOCALE, locale)
                                .build()
                    } else {
                        Timber.tag("429")
                                .i("access token expired, updating request without the token (should not happen!) and uid blank? ${tokenManager.userID}")
                        newRequest = request.newBuilder()
                                .header(HEADER_UID, tokenManager.userID)
                                .header(
                                    HEADER_API_VERSION,
                                    API_VERSION
                                )
                                .header(HEADER_APP_VERSION, appVersion)
                                .header(HEADER_USER_AGENT, userAgent)
                                .header(HEADER_LOCALE, locale)
                                .build()

                    }
                    // retry the original request which got 401 when we first tried it
                    return chain.proceed(newRequest)
                }
            }
            return null
        }

        return null
    }

    fun applyHeadersToRequest(request: Request): Request {

            val requestBuilder = request.newBuilder()
            // by default, we authorize requests using default user from UserManager
            requestBuilder.header(HEADER_UID, tokenManager.userID)
            if (tokenManager.authAccessToken != null) {
                requestBuilder.header(HEADER_AUTH, tokenManager.authAccessToken!!)
            }
            requestBuilder
                    .header(
                        HEADER_API_VERSION,
                        API_VERSION
                    )
                    .header(HEADER_APP_VERSION, appVersion)
                    .header(HEADER_USER_AGENT, userAgent)
                    .header(HEADER_LOCALE, locale)

            // we have to remove UID from those requests, because they mess up with server recognizing affected user
            if (request.url().toString().endsWith(AUTH_PATH) || request.url().toString().contains(AUTH_INFO_PATH)
            ) {
                requestBuilder.removeHeader(HEADER_AUTH).removeHeader(
                    HEADER_UID
                )
            }

            // we customize auth headers if different than default user has to be authorized
            request.tag(AuthTag::class.java)?.also {
                if (it.username == null) { // clear out default auth and unique session headers
                    requestBuilder.removeHeader(HEADER_AUTH)
                    requestBuilder.removeHeader(HEADER_UID)
                } else if (it.username != tokenManager.username) { // if it's the default user, credentials are already there
                    val userTokenManager = userManager.getTokenManager(it.username)
                    userTokenManager?.let { manager ->
                        if (manager.authAccessToken != null) {
                            Timber.d("setting non-default auth headers for ${it.username}")
                            requestBuilder.header(HEADER_AUTH, manager.authAccessToken!!)
                            requestBuilder.header(HEADER_UID, manager.userID)
                        }
                    }
                }
            }

            return requestBuilder.build()
        }
}