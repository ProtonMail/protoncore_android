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

package me.proton.android.core.domain.data

/**
 * Created by dinokadrikj on 4/20/20.
 *
 * This interface will serve as a base interface for the token manager, so that the other modules
 * or even the clients can provide their own implementation.
 */

// region fields
const val TOKEN_SCOPE_FULL = "full"
const val TOKEN_SCOPE_SELF = "self"
// endregion

data class TokenData(
    var userID: String? = null,
    // refresh token should not be null
    var refreshToken: String,
    var accessToken: String? = null,
    var scope: String? = TOKEN_SCOPE_FULL,
    var encPrivateKey: String? = null
)

interface TokenSettings {
    // region abstract fields
    val userID: String?
    val refreshToken: String
    val accessToken: String?
    val scope: String?
    val privateKey: String?
    // endregion

    /**
     * Clears the token related preferences.
     */
    fun clear()

    /**
     * Saves the token related preferences.
     */
    fun persist(tokenData: TokenData)

    /**
     * Creates a [TokenData] data class with the values that are already loaded from the settings/storage.
     * Having data class that wraps all of them is much easier for manipulation. However, if the client
     * prefers to use the values without [TokenData] it is free to use it.
     */
    fun load(): TokenData {
        return TokenData(userID, refreshToken, accessToken, scope,  privateKey)
    }
}