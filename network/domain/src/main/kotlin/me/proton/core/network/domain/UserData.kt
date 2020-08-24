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
package me.proton.core.network.domain

import me.proton.core.network.domain.humanverification.HumanVerificationHeaders

/**
 * Getters and setters for user info bound to a given [ApiManager] instance.
 */
interface UserData {

    /**
     * Session UID to be used for 'x-pm-uid' header and various request bodies.
     */
    val sessionUid: String

    /**
     * Access token to be used for 'Authorization' header.
     */
    val accessToken: String

    /**
     * Refresh token to be used for access token refreshing.
     */
    val refreshToken: String

    /**
     * Update access and refresh tokens.
     */
    fun updateTokens(access: String, refresh: String)

    /**
     * Getter for `x-pm-human-verification-token-type` and `x-pm-human-verification-token`.
     */
    var humanVerificationHandler: HumanVerificationHeaders?

    /**
     * Tells client to force logout (because e.g. token refresh unrecoverably failed).
     */
    fun forceLogout()
}
