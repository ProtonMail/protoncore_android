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

import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.UserSettings

interface UserSettingsRepository {

    suspend fun setUsername(sessionUserId: SessionUserId, username: String): Boolean

    /**
     * Update [UserSettings], locally.
     *
     * Note: This function is usually used for Events handling.
     *
     * @throws IllegalStateException if corresponding user doesn't exist.
     */
    suspend fun updateUserSettings(
        userSettings: UserSettings
    )

    /**
     * Get [UserSettings], using [sessionUserId].
     *
     * @return values emitted from cache/disk, and also from fetcher if [refresh] is true.
     */
    fun getUserSettingsFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<UserSettings>>


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
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String
    ): UserSettings

    /**
     * Updates user's login password.
     */
    suspend fun updateLoginPassword(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String,
        auth: Auth
    ): UserSettings

    /**
     * Updates user's telemetry
     */
    suspend fun updateTelemetry(
        userId: UserId,
        isEnabled: Boolean,
    ): UserSettings

    /**
     * Updates user's crash report
     */
    suspend fun updateCrashReports(
        userId: UserId,
        isEnabled: Boolean,
    ): UserSettings

    fun markAsStale(userId: UserId)
}
