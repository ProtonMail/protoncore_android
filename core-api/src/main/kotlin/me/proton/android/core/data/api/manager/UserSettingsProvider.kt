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
import ch.protonmail.libs.core.preferences.*
import me.proton.android.core.domain.data.AppSettings
import me.proton.android.core.domain.data.UserSettings
import me.proton.android.core.domain.entity.Keys
import me.proton.android.core.domain.entity.User

/**
 * Created by dinokadrikj on 4/23/20.
 *
 * This class provides all data for a single user
 */

const val PREF_USERNAME = "username"

class UserSettingsProvider(override var preferences: SharedPreferences): UserSettings,
    AppSettings, PreferencesProvider {

    override val userName: String? by preferences.string()
    override val keys: List<Keys>? by preferences.list()

    /**
     * We return the loaded user username if the app does not support multi-user.
     */
    override val activeUsername: String by preferences.string(key = PREF_USERNAME, default = userName!!)

    /**
     * The username of the wanted user (useful for multi-user clients).
     */
    override fun load(username: String): User? {
        return if (userName.isNullOrBlank()) null else User(
            userName!!,
            keys
        )
    }

}