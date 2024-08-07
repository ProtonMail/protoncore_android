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

package me.proton.core.userrecovery.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import me.proton.core.usersettings.domain.repository.UserSettingsRemoteDataSource
import javax.inject.Inject

/**
 * Remotely set a newly generated user primary recovery secret.
 * Note: can be called only for [private][me.proton.core.user.domain.entity.User.private] users.
 */
class SetRecoverySecretRemote @Inject constructor(
    private val eventManagerProvider: EventManagerProvider,
    private val getRecoverySecret: GetRecoverySecret,
    private val userSettingsRemoteDataSource: UserSettingsRemoteDataSource
) {
    suspend operator fun invoke(
        userId: UserId
    ) {
        eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            val (secret, signature) = getRecoverySecret(userId)
            userSettingsRemoteDataSource.setRecoverySecret(userId, secret, signature)
        }
    }
}
