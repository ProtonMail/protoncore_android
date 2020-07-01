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

package me.proton.android.core.data.api.manager

import me.proton.android.core.data.api.entity.request.RefreshBody
import me.proton.android.core.data.api.entity.response.RefreshResponse
import me.proton.android.core.domain.data.TOKEN_SCOPE_FULL
import me.proton.android.core.domain.data.TokenData
import me.proton.android.core.domain.data.TokenSettings

/**
 * Created by dinokadrikj on 4/16/20.
 *
 * TokenManager stores separate credentials for every user in [SecureSharedPreferences] file.
 */

// region constants api
const val TOKEN_TYPE = "Bearer"
// endregion

/**
 * Token Manager class that keeps track of a tokens (access, refresh token etc) per username.
 * There could be more than one [TokenManager] instances per application, one per user (logged in)
 * in a multi-user apps. If the application supports only single user then always there will be
 * single instance of this [TokenManager].
 */
class TokenManager private constructor(val username: String,
                                       private val settings: TokenSettings) {


    // load the data from the settings
    private val tokenData: TokenData = settings.load()

    val userID: String
        get() = tokenData.userID ?: ""

    var privateKey: String?
        get() = tokenData.encPrivateKey
        set(value) {
            tokenData.encPrivateKey = value
            persist()
        }

    val authAccessToken: String?
        get() = if (tokenData.accessToken.isNullOrBlank()) null else {
            "$TOKEN_TYPE $tokenData.accessToken" // concatenation
        }

    private fun persist() {
        settings.persist(tokenData)
    }

    fun handleRefresh(response: RefreshResponse) {
        if (response.refreshToken != null) {
            tokenData.refreshToken = response.refreshToken
        }
        if (!response.privateKey.isNullOrEmpty()) {
            tokenData.encPrivateKey = response.privateKey
        }
        tokenData.accessToken = response.accessToken
        tokenData.scope = response.scope ?: TOKEN_SCOPE_FULL // TODO: check this if token full scope is the right default value here
        persist()
    }

    fun createRefreshBody(): RefreshBody {
        return RefreshBody(
            refreshToken = tokenData.refreshToken
        )
    }

    /**
     * FIXME: should be checked if this method is really needed.
     */
    fun clearAccessToken() {
        tokenData.accessToken = null
        tokenData.scope = ""
    }

    /**
     * Clears the settings for this token data.
     */
    fun clear() {
        settings.clear()
        settings.load()
    }

    companion object {

        private val tokenManagers = mutableMapOf<String, TokenManager>()

        @Synchronized
        fun clearInstance(username: String) {
            if (username.isNotEmpty()) tokenManagers.remove(username)
        }

        @Synchronized
        fun clearAllInstances() {
            tokenManagers.clear()
        }

        fun removeEmptyTokenManagers() {
            tokenManagers.remove("")
        }

        /**
         * Creates and caches instances of TokenManager with support for a [TokenManager] instance
         * per user (username).
         */
        @Synchronized
        fun getInstance(username: String, settings: TokenSettings): TokenManager? {
            return if (!username.isBlank()) tokenManagers.getOrPut(username) {
                TokenManager(
                    username,
                    settings
                )
            } else null
        }
    }
}