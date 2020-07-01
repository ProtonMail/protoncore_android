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
import me.proton.android.core.domain.entity.User
import java.util.HashMap

/**
 * Created by dinokadrikj on 4/22/20.
 */
class UserManager(
    override val preferences: SharedPreferences): PreferencesProvider {

    // TODO: DI is needed for this
    private val tokenSettingsProvider =
        TokenSettingsProvider(preferences)
    private val userSettingsProvider =
        UserSettingsProvider(preferences)
    private val userReferences = HashMap<String, User>()

    /**
     * @return username of currently "active" user
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    val username: String
        get() = userSettingsProvider.activeUsername

    /**
     * Use this method to get User's settings for other users than currently active.
     *
     * @return [User] object for given username, might have empty values if user was not saved before
     */
    @Synchronized
    fun getUser(username: String): User {
        if (!userReferences.containsKey(username)) {
            val newUser = userSettingsProvider.load(username)
            if (!username.isBlank() ) {
                userReferences[username] =  newUser!!
            }
            return newUser!!
        }
        return userReferences[username]!!
    }

    fun getTokenManager(username: String): TokenManager? {
        val tokenManager =
            TokenManager.getInstance(
                username,
                tokenSettingsProvider
            )
        // make sure the private key is here
        tokenManager?.let {
            if (it.privateKey.isNullOrBlank()) {
                val user = getUser(username)
                user.keys?.let { keys ->
                    for (key in keys) {
                        if (key.primary == 1) {
                            it.privateKey = key.privateKey // it's needed for verification later
                            break
                        }
                    }
                }
            }
        }
        return tokenManager
    }

    /**
     * Logout the user with the [username] with or without making api call. We should be able to be
     * able to logout anytime (even without having connectivity).
     */
    fun logout(makeApiCall: Boolean, username: String) {

    }
}