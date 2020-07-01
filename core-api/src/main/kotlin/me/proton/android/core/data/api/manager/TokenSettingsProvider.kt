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

import android.content.SharedPreferences
import ch.protonmail.libs.core.preferences.PreferencesProvider
import ch.protonmail.libs.core.preferences.string
import me.proton.android.core.domain.data.TOKEN_SCOPE_FULL
import me.proton.android.core.domain.data.TokenData
import me.proton.android.core.domain.data.TokenSettings

// region constants preferences
private const val PREF_ENC_PRIVATE_KEY = "priv_key"
private const val PREF_REFRESH_TOKEN = "refresh_token"
private const val PREF_USER_UID = "user_uid"
private const val PREF_ACCESS_TOKEN = "access_token_plain"
private const val PREF_TOKEN_SCOPE = "access_token_scope"
// endregion

/**
 * Created by dinokadrikj on 4/22/20.
 */
class TokenSettingsProvider(override val preferences: SharedPreferences): TokenSettings, PreferencesProvider {

    override val userID by string(PREF_USER_UID)
    override val refreshToken by string(PREF_REFRESH_TOKEN)
    override val accessToken by string(PREF_ACCESS_TOKEN)
    override val scope by string(default = TOKEN_SCOPE_FULL, key = PREF_TOKEN_SCOPE)
    override val privateKey by string(PREF_ENC_PRIVATE_KEY)

    override fun clear() {
        preferences.edit()
            .remove(PREF_USER_UID)
            .remove(PREF_REFRESH_TOKEN)
            .remove(PREF_ACCESS_TOKEN)
            .remove(PREF_TOKEN_SCOPE)
            .remove(PREF_ENC_PRIVATE_KEY).apply()
    }

    override fun persist(tokenData: TokenData) {
        preferences.edit()
            .putString(PREF_USER_UID, userID)
            .putString(PREF_REFRESH_TOKEN, refreshToken)
            .putString(PREF_ACCESS_TOKEN, accessToken)
            .putString(PREF_ENC_PRIVATE_KEY, privateKey)
            .apply()
    }
}