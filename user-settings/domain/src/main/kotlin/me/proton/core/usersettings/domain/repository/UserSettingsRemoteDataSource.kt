/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

import me.proton.core.auth.domain.entity.ServerProof
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.entity.UserSettingsProperty

interface UserSettingsRemoteDataSource {
    suspend fun fetch(userId: UserId): UserSettings
    suspend fun setUsername(userId: UserId, username: String): Boolean
    suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String
    ): Pair<UserSettings, ServerProof>

    suspend fun updateLoginPassword(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String,
        auth: Auth
    ): Pair<UserSettings, ServerProof>

    suspend fun updateUserSettings(userId: UserId, property: UserSettingsProperty): UserSettings
}
