/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.domain.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.usersettings.domain.entity.UserSettings

interface UserSettingsRepository {

    suspend fun setUsername(sessionUserId: SessionUserId, username: String): Boolean

    /**
     * Returns the general settings for the user.
     */
    suspend fun getUserSettings(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): UserSettings

    /**
     * Updates user's recovery email.
     */
    suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String,
        secondFactorCode: String
    ): UserSettings
}
