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

package me.proton.core.usersettings.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import me.proton.core.usersettings.domain.entity.UserSettingsProperty
import me.proton.core.usersettings.domain.repository.UserSettingsRemoteDataSource
import javax.inject.Inject

class UpdateUserSettingsRemote @Inject constructor(
    private val eventManagerProvider: EventManagerProvider,
    private val userSettingsRemoteDataSource: UserSettingsRemoteDataSource,
) {
    suspend operator fun invoke(
        userId: UserId,
        settingsProperty: UserSettingsProperty
    ) {
        // Prevent the event loop to override the updated value.
        eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            userSettingsRemoteDataSource.updateUserSettings(userId, settingsProperty)
        }
    }
}
