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

package me.proton.core.usersettings.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.data.extension.fromEntity
import me.proton.core.usersettings.data.extension.toEntity
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsLocalDataSource
import javax.inject.Inject

class UserSettingsLocalDataSourceImpl @Inject constructor(
    db: UserSettingsDatabase
) : UserSettingsLocalDataSource {

    private val userSettingsDao = db.userSettingsDao()

    override fun observeByUserId(userId: UserId): Flow<UserSettings?> =
        userSettingsDao.observeByUserId(userId).map { it?.fromEntity() }

    override suspend fun insertOrUpdate(settings: UserSettings) =
        userSettingsDao.insertOrUpdate(settings.toEntity())

    override suspend fun delete(userId: UserId) =
        userSettingsDao.delete(userId)

    override suspend fun deleteAll() =
        userSettingsDao.deleteAll()
}
