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

package me.proton.core.settings.data.api.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.settings.data.api.UserSettingsApi
import me.proton.core.settings.data.api.request.UpdateRecoveryEmailRequest
import me.proton.core.settings.domain.repository.UserSettingsRepository

class UserSettingsRepositoryImpl(
    private val provider: ApiProvider
) : UserSettingsRepository {

    override suspend fun getSettings(sessionUserId: SessionUserId) =
        provider.get<UserSettingsApi>(sessionUserId).invoke {
            getSettings().toUserSettings()
        }.valueOrThrow

    override suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String,
        twoFactorCode: String
    ) = provider.get<UserSettingsApi>(sessionUserId).invoke {
        updateRecoveryEmail(
            UpdateRecoveryEmailRequest(
                email = email,
                twoFactorCode = twoFactorCode,
                clientEphemeral = clientEphemeral,
                clientProof = clientProof,
                srpSession = srpSession
            )
        ).toUserSettings()
    }.valueOrThrow
}
